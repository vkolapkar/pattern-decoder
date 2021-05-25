import java.io.*;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Scanner;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class ExcelUtility1 {

	public static void main(String[] args) {

		writeToExcelFile(readExcelFile());
	}

	private static String formatErrorCode(String str) {

		String result = "";
		String[] tokens = str.split("\\.");

		switch(tokens[1].length()) {

		case 1: tokens[0] = tokens[0].concat("E000");
		result = tokens[0]+tokens[1]+tokens[2];
		return result;

		case 2: tokens[0] = tokens[0].concat("E00");
		result = tokens[0]+tokens[1]+tokens[2];
		return result;

		case 3: tokens[0] = tokens[0].concat("E0");
		result = tokens[0]+tokens[1]+tokens[2];
		return result;

		default: System.out.println("tokens[1] length: "+tokens[1].length());
		break;
		}

		return "";

	}

	private static List<String> readExcelFile() {

		String str = "";
		List<String> formattedCodes = new ArrayList<String>();

		try
		{
			FileInputStream file = new FileInputStream(new File("SPS Business Rules Repository.xlsx"));

			//Create Workbook instance holding reference to .xlsx file
			XSSFWorkbook workbook = new XSSFWorkbook(file);

			//Get first/desired sheet from the workbook
			XSSFSheet sheet = workbook.getSheet("Business Rules");

			for(int rownum = 1; rownum < 130; rownum++)
			{
				Row row = sheet.getRow(rownum);
				Cell cell = row.getCell(14);
				str = formatErrorCode(cell.getStringCellValue());
				formattedCodes.add(str);
			}
			file.close();
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}

		return formattedCodes;
	}

	private static void writeToExcelFile(List<String> strList) {

		//Blank workbook
		XSSFWorkbook workbook = new XSSFWorkbook(); 

		//Create a blank sheet
		XSSFSheet sheet = workbook.createSheet("Sheet1");

		int rownum = 0;
		int cellnum = 0;

		Row row;
		Cell cell;

		for (String str : strList)
		{
			row = sheet.createRow(rownum++);
			cell = row.createCell(cellnum);

			cell.setCellValue(str);
		}
		try
		{
			//Write the workbook in file system
			FileOutputStream out = new FileOutputStream(new File("FormattedErrorCodes.xlsx"));
			workbook.write(out);
			out.close();
			System.out.println("File written successfully on disk.");
		} 
		catch (Exception e) 
		{
			e.printStackTrace();
		}
	}
}
