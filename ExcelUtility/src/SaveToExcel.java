import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.opencsv.CSVWriter;

public class SaveToExcel {

	static List<String[]> strList = new ArrayList<String[]>();

	public static void main(String[] args) throws IOException {

		File file = new File("A:\\eclipse_workspace\\ExcelUtility\\SPSRules.txt");
		Scanner sc = new Scanner(file);

		int i = 1, beginIndex, endIndex;

		while (sc.hasNextLine()) {
			
			int lastIndex;
			String str = "";
			String[] tokens;
			String[] strArray = new String[6];

			if(i%2 == 0) {
				tokens = sc.nextLine().split(",");

				//number of headers = 14. If length is greater than 14, that indicates there are commas in the RULE_DESC
				if(tokens.length > 14) {

					lastIndex = tokens.length - 10;

					for(int j = 3; j < (lastIndex-1); j++) {
						str = str + tokens[j];
						str = str + ",";
					}

					str = str + tokens[lastIndex-1];

					strArray[0] = tokens[0].substring(tokens[0].indexOf("'"), tokens[0].lastIndexOf("'"))+"'";
					strArray[1] = tokens[1];
					strArray[2] = tokens[2];
					strArray[3] = str.toString();
					strArray[4] = tokens[tokens.length-9];
					strArray[5] = tokens[tokens.length-8];

					strList.add(strArray);

				} 
				else {

					beginIndex = tokens[0].indexOf("'");
					endIndex = tokens[0].lastIndexOf("'");

					strArray[0] = tokens[0].substring(beginIndex, endIndex)+"'";
					strArray[1] = tokens[1];
					strArray[2] = tokens[2];
					strArray[3] = tokens[3];
					strArray[4] = tokens[5];
					strArray[5] = tokens[6];

					strList.add(strArray);
				}
			}
			else {

				String dummyStr = sc.nextLine();
			}

			i++;
		}

		writeToCSV(strList);
	}

	private static void writeToCSV(List<String[]> strList) throws IOException {

		OutputStream out = new FileOutputStream("SPS_Rules1.csv");
		OutputStreamWriter outputStreamWriter = new OutputStreamWriter(out);
		CSVWriter csvWriter = new CSVWriter(outputStreamWriter);
		String[] headers = new String[]{"ERR_CD", "RULE_TYPE_CD", "RULE_CTGRY_CD", "RULE_DESC", "SRC_SYS_CD", "SVRTY_LVL_CD"};
		csvWriter.writeNext(headers);
		csvWriter.writeAll(strList);
		csvWriter.close();
	}
}
