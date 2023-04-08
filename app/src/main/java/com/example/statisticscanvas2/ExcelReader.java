package com.example.statisticscanvas2;

import android.content.Context;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;

import org.apache.poi.xssf.usermodel.XSSFRow;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;


class ExcelReader{
    private final XSSFWorkbook workbook;

    ExcelReader(String path, Context context) throws IOException {
        File file = new File(context.getFilesDir(), path);

        FileInputStream fIn = new FileInputStream(file);
        workbook = new XSSFWorkbook(fIn);

        fIn.close();
    }
    public double[] getNumberListFromColumnInCell(String colName, int sheetNum) throws IllegalStateException {
        XSSFSheet sheet = workbook.getSheetAt(sheetNum);
        int colNum = mFindHeaderIn(sheet, colName);

        if(colNum == -1){
            return null;
        }

        double[] doubles = new double[sheet.getLastRowNum()];
        Arrays.fill(doubles, Double.MAX_VALUE);

        for(int i=1;i<sheet.getPhysicalNumberOfRows();i++){
            XSSFRow row = sheet.getRow(i);
            if(row == null || row.getCell(colNum) == null){
                continue;
            }

            doubles[i - 1] = row.getCell(colNum).getNumericCellValue();
        }

        int cutoff = -1;
        for(int i=doubles.length -1;i>=0;i--){
            if(doubles[i] != Double.MAX_VALUE){
                cutoff = i;
                break;
            }
        }

        return Arrays.copyOfRange(doubles, 0, cutoff + 1);
    }

    public String[] getStringListFromColumnInCell(String colName, int sheetNum) {
        XSSFSheet sheet = workbook.getSheetAt(sheetNum);
        int colNum = mFindHeaderIn(sheet, colName);

        if(colNum == -1){
            return null;
        }

        String[] strings = new String[sheet.getLastRowNum()];

        for(int i=1;i<sheet.getPhysicalNumberOfRows();i++){
            XSSFRow row = sheet.getRow(i);

            if(row == null || row.getCell(colNum) == null){
                continue;
            }

            strings[i - 1] = row.getCell(colNum).getStringCellValue().trim();
        }

        int cutoff = -1;
        for(int i=strings.length -1;i>=0;i--){
            if(strings[i] != null){
                cutoff = i;
                break;
            }
        }

        return Arrays.copyOfRange(strings, 0, cutoff + 1);
    }

    public Object[] query(int sheetNum, String query, String colName){
        String[] queries;
        if(!query.contains("&")){
            queries = new String[1];
            queries[0] = query;
        }
        else{
            queries = query.split(" & ");
        }

        XSSFSheet sheet = workbook.getSheetAt(sheetNum);
        int numberOfRows = sheet.getPhysicalNumberOfRows();
        List<Integer> indices = new ArrayList<>(numberOfRows);

        for(int i=1;i<numberOfRows;i++){
            indices.add(i);
        }

        int colNum = mFindHeaderIn(sheet, colName);
        CellType colType  = mGetColumnTypeIn(sheet, colName);

        for(String q : queries){
            String[] components = q.split(" ");

            switch (components[0].toLowerCase()){
                case "first":
                    // Ex: First 3
                    indices = indices.subList(0, Integer.parseInt(components[1]));
                    break;
                case "from":
                    // Ex: From 3 to 4
                    indices = indices.subList(Integer.parseInt(components[1]) - 1, Integer.parseInt(components[1]));
                    break;
                case "with":
                    // Ex: with Salary > 10k

                    CellType refColType = mGetColumnTypeIn(sheet, components[1]);

                    if(refColType == CellType.NUMERIC){
                        indices = mOperatorQueryHandlerForNumberColumn(components[2], components[3], indices,
                                sheetNum, components[1]);
                    }
                    else if(refColType == CellType.STRING || refColType == CellType.BOOLEAN){
                        indices = mOperatorQueryHandlerForStringColumn(components[2], components[3], indices,
                                sheetNum, components[1]);
                    }
                    break;
                case "sort":
                    int sortByColNum = colNum;
                    CellType refColType2 = colType;

                    if(components.length == 2){
                        sortByColNum = mFindHeaderIn(sheet, components[1]);
                        refColType2 = mGetColumnTypeIn(sheet, components[1]);
                    }

                    List<Item> items = new ArrayList<>();

                    for(int i : indices){
                        try{
                            if(refColType2 == CellType.NUMERIC){
                                items.add(new Item(sheet.getRow(i).getCell(sortByColNum).getNumericCellValue(), i));
                            }
                            else{
                                items.add(new Item(sheet.getRow(i).getCell(sortByColNum).getStringCellValue(), i));
                            }
                        }
                        catch (Exception e){
                            break;
                        }
                    }

                    Collections.sort(items);
                    indices.clear();

                    for(Item item : items){
                        indices.add(item.position);
                    }
                    break;
                default:
                    // Ex: >= 35k;

                    if(colType == CellType.NUMERIC){
                        indices = mOperatorQueryHandlerForNumberColumn(components[0], components[1], indices,
                                sheetNum, colName);
                    }
                    else if(colType == CellType.STRING || colType == CellType.BOOLEAN){
                        indices = mOperatorQueryHandlerForStringColumn(components[0], components[1], indices,
                                sheetNum, colName);
                    }
            }
        }

        Object[] data = new Object[indices.size()];

        for(int i=0;i<indices.size();i++){
            int index = indices.get(i);

            if(colType == CellType.NUMERIC){
                data[i] = sheet.getRow(index).getCell(colNum).getNumericCellValue();
            }
            else{
                data[i] = sheet.getRow(index).getCell(colNum).getStringCellValue();
            }
        }

        return data;
    }

    public String getStringAt(int sheetNum, int rowNum, String colName){
        int colNum = mFindHeaderIn(workbook.getSheetAt(sheetNum), colName);
        String original = workbook.getSheetAt(sheetNum).getRow(rowNum + 1).getCell(colNum).getStringCellValue();

        return original.trim();
    }

    private List<Integer> mOperatorQueryHandlerForStringColumn(String operator, String searchTerm,
                                                               List<Integer> indices, int sheetNum, String colName){
        XSSFSheet sheet = workbook.getSheetAt(sheetNum);
        int colNum = mFindHeaderIn(sheet, colName);

        List<Integer> ints = new ArrayList<>();

        for(int i : indices){
            Row row = sheet.getRow(i);

            if(row == null || row.getCell(colNum) == null){
                continue;
            }

            String value = row.getCell(colNum).getStringCellValue();

            if(mConditionMatched(operator, value.compareTo(searchTerm))){
                ints.add(i);
            }
        }

        return ints;
    }

    private List<Integer> mOperatorQueryHandlerForNumberColumn(String operator, String searchTerm,
                                                               List<Integer> indices, int sheetNum, String colName){
        XSSFSheet sheet = workbook.getSheetAt(sheetNum);
        int colNum = mFindHeaderIn(sheet, colName);

        List<Integer> ints = new ArrayList<>();

        for(int i : indices){
            Row row = sheet.getRow(i);

            if(row == null || row.getCell(colNum) == null){
                continue;
            }

            double value = row.getCell(colNum).getNumericCellValue();

            if(mConditionMatched(operator, (int)(value - Double.parseDouble(searchTerm)))){
                ints.add(i);
            }
        }

        return ints;
    }

    private boolean mConditionMatched(String operator, int compareResult){
        return (compareResult < 0 && operator.contains("<")) ||
                (compareResult == 0 && operator.contains("=")) ||
                (compareResult > 0 && operator.contains(">"));
    }

    private int mFindHeaderIn(XSSFSheet sheet, String headerName){
        XSSFRow headerRow = sheet.getRow(0);

        int colNum = -1;
        for(Cell cell : headerRow){
            String columnName = cell.getStringCellValue();

            if(columnName.equals(headerName)){
                colNum = cell.getColumnIndex();
                break;
            }
        }

        return colNum;
    }

    private CellType mGetColumnTypeIn(XSSFSheet sheet, String colName){
        int colNum = mFindHeaderIn(sheet, colName);

        return sheet.getRow(1).getCell(colNum).getCellType();
    }

    private static class Item implements Comparable<Item>{
        Object value;
        int position;

        Item(Object value, int position){
            this.value = value;
            this.position = position;
        }

        @Override
        public int compareTo(Item o) {
            if(value instanceof String){
                return ((String)value).compareTo((String) o.value);
            }
            else{
                return (int)((Double) value - (Double) o.value);
            }
        }
    }
}