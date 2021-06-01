package com.ns.util;

import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternDecoderMain {

	private static Map<String, String> keyValMap = new ConcurrentHashMap<>();
	private static Map<String, String> patternsMap = new HashMap<>();

	Properties prop = null;

	private static String ruleToErrorCodeMappingConst = "RuleToErrorCodeMapping.";
	private static String getErrorCodeConst = "getErrorCode()";
	private static String errorCodeConstantsConst = "ErrorCodeConstants.";
	private static String noFieldSpecifiedConst = "NO_FIELD_SPECIFIED";
	private static String errorMessageConstantsConst = "ErrorMessageConstants.";
	private static String errorConstantsConst = "ErrorConstants.";
	private static String fbdaConstantConst = "FbdaConstant.";

	public static void main(String[] args) {

		PatternDecoderMain obj = new PatternDecoderMain();
		obj.startupTask();
	}

	private void startupTask() {

		//load all user-defined patterns
		try {
			prop = readPropertiesFile("A:\\eclipse_workspace\\pattern-decoder-2\\src\\resources\\config.properties");

			patternsMap.put("1", prop.getProperty("pattern1"));
			patternsMap.put("2", prop.getProperty("pattern2"));
			patternsMap.put("3", prop.getProperty("pattern3"));
			patternsMap.put("4", prop.getProperty("pattern4"));
			patternsMap.put("5", prop.getProperty("pattern5"));
			patternsMap.put("6", prop.getProperty("pattern6"));
			patternsMap.put("7", prop.getProperty("pattern7"));
			patternsMap.put("8", prop.getProperty("pattern8"));
			patternsMap.put("9", prop.getProperty("pattern9"));

		} catch (IOException e) {
			e.printStackTrace();
		}

		readAllFilesAndCreateMap();
		assessFilesForErrorCodeNErrorMsg();
	}

	private void readAllFilesAndCreateMap() {

		try {
			Files.walk(Paths.get(prop.getProperty("sourceFolderUtil"))).filter(path -> !path.toFile().isDirectory()).parallel()
			.forEach(this::readFile);

		} catch (IOException e) {
			System.out.println("Exception in load RuleError Code map");
			e.printStackTrace();
		}
	}

	/**
	 * Read file line and convert it in to key value format and store in global map
	 * e.g. I/P public static final String DATA_VALIDATION_CODE = "8000"; O/P Key:
	 * DATA_VALIDATION_CODE Value : "8000
	 * 
	 * I/P RULE_50 Value : "RLE005000" O/P Key: RULE_51 Value : "RLE005000"
	 */
	private void readFile(Path path) {

		Map<String, String> codeMsg = null;
		try {
			codeMsg = Files.lines(path).filter(line -> (isConstantDeclarationPattern(line) || isErrorCodeEnumPattern(line)))
					.map(this::normalize)
					.collect(Collectors.toMap(keyVal -> getKey(keyVal[0].strip()), keyVal -> keyVal[1]));

		} catch (IOException e) {

			e.printStackTrace();
		}
		keyValMap.putAll(codeMsg);
	}

	private String getKey(String key) {
		if(key.strip().contains(" ")) {
			String formattedKey  =key.trim().substring(key.lastIndexOf(" ")+1, key.length());
			return formattedKey.strip() ;
		}
		return key;
	}

	private String[] normalize(String line) {
		String[] keyVal = new String[2];
		if (isConstantDeclarationPattern(line)) {
			keyVal = line.strip().replace(";", "").split("=");
		} else if (isErrorCodeEnumPattern(line)) {
			keyVal = line.strip().replace("(", "=").replace("),", "").replace(");", "").split("=");
		}
		return keyVal;
	}

	private boolean isConstantDeclarationPattern(String line) {
		Pattern pattern = Pattern.compile(prop.getProperty("errorMessageDeclaration"));
		return pattern.matcher(line).find();
	}

	private boolean isErrorCodeEnumPattern(String line) {
		Pattern pattern = Pattern.compile(prop.getProperty("errorCodeDeclaration"));
		return pattern.matcher(line).find();
	}

	private String getStrippedString(String key) {

		String keyStripped=key;
		while(keyStripped.startsWith(" ")) {
			keyStripped= keyStripped.replaceFirst(" ","");
		}
		return keyStripped.trim();
	}

	// read Files from Source Folder
	private void assessFilesForErrorCodeNErrorMsg() {

		try {
			Files.walk(Paths.get(prop.getProperty("sourceFolder")))
			.filter(path -> (!path.toFile().isDirectory() && path.toString().endsWith(".java"))).parallel()
			.forEach(this::assessFile);

		} catch (IOException e) {
			System.out.println("Exception in load RuleError Code map");
			e.printStackTrace();
		}
	}

	//store parsed error codes and error messages returned by errorCodeNMsg() in hashMap
	private void assessFile(Path path) {

		Map<String, String> codeMsg = null;
		try {
			codeMsg = Files.lines(path).map(this::checkForExpectedPattern)
					.filter(tokenArray -> Integer.parseInt(tokenArray[0]) > 0).map(this::errorCodeNMsg)
					.collect(Collectors.toMap(keyVal -> keyVal[0], keyVal -> keyVal[1], (existingVal, newVal) -> {
						if (existingVal.contains(newVal))
							return existingVal;
						else
							return existingVal + " \n" + newVal;
					}
							));

			System.out.println("########### File " + path.toFile().getName());
			if (!codeMsg.isEmpty())
				System.out.println("Code VS Messages");
			codeMsg.entrySet()
			.forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value : " + entry.getValue()));

		} catch (IOException e) {

			e.printStackTrace();
		}
	}

	//Check if any of the patterns from the HashMap are present in the input line
	private String[] checkForExpectedPattern(String line) {
		int patternType = 0;
		boolean check = false;

		String[] tokens;

		for (Map.Entry<String, String> set : patternsMap.entrySet()) {

			tokens = set.getValue().split("\\|");

			for(String str : tokens) {

				if(str.charAt(0) == '!') {
					str = str.substring(1);
					check = !patternMatcher(line, str);
				}
				else {
					if(patternMatcher(line, str)) check = true;
					else {
						check = false;
						break;
					}
				}				
			}

			if(check) {
				patternType = Integer.valueOf(set.getKey());
				break;
			}
		}

		return new String[] { patternType + "", line };
	}

	//returns true if token is present in input line, else false
	private boolean patternMatcher(String line,String token) {
		Pattern pattern = Pattern.compile(token);
		return pattern.matcher(line).find();
	}

	//	 As per User pre Defined patterns pick up the respective matching pattern and parse it
	private String[] errorCodeNMsg(String[] tokenArr) {

		String[] errorCodeMsgArr = new String[2];
		int patternType = Integer.parseInt(tokenArr[0]);
		String line = tokenArr[1];

		switch (patternType) {
		case 1: errorCodeMsgArr = parsePattern1(line);
		break;

		case 2: errorCodeMsgArr = parsePattern2(line);
		break;

		case 3: errorCodeMsgArr = parsePattern3(line);
		break;

		case 4: errorCodeMsgArr = parsePattern4(line);
		break;		

		case 5: errorCodeMsgArr = parsePattern5(line);
		break;

		case 6: errorCodeMsgArr = parsePattern6(line);
		break;

		case 7: errorCodeMsgArr = parsePattern7(line);
		break;

		case 8: errorCodeMsgArr = parsePattern8(line);
		break;		

		case 9: errorCodeMsgArr = parsePattern9(line);
		break;

		default: break;
		}

		return errorCodeMsgArr;
	}

	private String[] parsePattern1(String line) {

		String errorCode = null;
		String errorMessage = null;
		String errorMessageKey=null;
		String messagePlaceHolder = "_ERROR_MESSAGE";

		String ruleKey =line.substring(line.indexOf(ruleToErrorCodeMappingConst) + ruleToErrorCodeMappingConst.length(), line.indexOf(getErrorCodeConst));

		int index=line.indexOf(messagePlaceHolder);
		if(index==-1){
			index = line.indexOf("_MESSAGE");
			messagePlaceHolder = "_MESSAGE";
		}

		errorMessageKey = line.substring(line.indexOf(errorCodeConstantsConst) + errorCodeConstantsConst.length(), index);
		errorCode=keyValMap.get(ruleKey);

		errorMessageKey=getStrippedString(errorMessageKey)+messagePlaceHolder;
		errorMessage =keyValMap.get(errorMessageKey);

		return new String[] {errorCode+","+errorMessageKey,errorMessage};
	}

	private String[] parsePattern2(String line) {

		String errorCode=null;
		String errorMessage=null; 
		String ruleKey =line.substring(line.indexOf(ruleToErrorCodeMappingConst) + prop.getProperty(ruleToErrorCodeMappingConst).length(), line.indexOf(getErrorCodeConst));
		errorCode =keyValMap.get(ruleKey);

		line=line.substring(line.indexOf(getErrorCodeConst) + getErrorCodeConst.length());
		errorMessage =line.substring(line.indexOf(","), line.indexOf("\","));	

		return new String[] {errorCode+", "+noFieldSpecifiedConst,getStrippedString(errorMessage)};
	}

	private String[] parsePattern3(String line) {

		String errorCode=null;
		String errorMessage=null; 

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.indexOf("+"));
		errorCode =keyValMap.get(getStrippedString(ruleKey));

		String errorMessageKey =line.substring(line.lastIndexOf(errorMessageConstantsConst)+errorMessageConstantsConst.length(),line.indexOf(")"));
		errorMessage =keyValMap.get(getStrippedString(errorMessageKey));

		return new String[] {errorCode+","+errorMessageKey,errorMessage};
	}

	private String[] parsePattern4(String line) {

		String errorCode=null;
		String errorMessage=null; 

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.indexOf("+"));
		errorCode =keyValMap.get(getStrippedString(ruleKey));

		String errorMessageKey =line.substring(line.lastIndexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.indexOf(")"));
		errorMessage =keyValMap.get(getStrippedString(errorMessageKey));

		return new String[] {errorCode+","+errorMessageKey,errorMessage};
	}

	private String[] parsePattern5(String line) {

		String errorCode=null;
		String errorMessage=null;

		if(line.equals("ErrorLoggingUtility.populateChangeDetectErrorCollection(errorCollection, ErrorCodeConstants.RULE_T3,\"Reqest Business Keys matches to records\" + GroupingRelatedProviderAddressProgram.ENTITY_NAME + \":\" + \" Request :\" + reqView.toString() + \" SPS :\" + dbData.toString(), null, Long.valueOf(entityIdMap.get(SPSCommonConstant.ENTITY_ID.RLTD_MSTR_PROV_ID.toString())), userId, reqView, \"detectChange\", CLASS_NAME, reqView.getGroupingProgramKey());"))
			line = "ErrorLoggingUtility.populateChangeDetectErrorCollection(errorCollection, ErrorCodeConstants.RULE_T3, \"Reqest Business Keys matches to records\" + GroupingRelatedProviderAddressProgram.ENTITY_NAME + \":\" + \" Request :\" + reqView.toString() + \" SPS :\" + dbData.toString(), null, Long.valueOf(entityIdMap.get(SPSCommonConstant.ENTITY_ID.RLTD_MSTR_PROV_ID.toString())), userId, reqView, \"detectChange\", CLASS_NAME, reqView.getGroupingProgramKey());";

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.indexOf(", \""));
		errorCode =keyValMap.get(ruleKey);

		errorMessage=(line.substring(line.indexOf(errorCodeConstantsConst+ruleKey)+(errorCodeConstantsConst+ruleKey+",").length(), line.indexOf("\" + "))).substring(1);

		return new String[] {errorCode+", "+noFieldSpecifiedConst,getStrippedString(errorMessage)};
	}

	private String[] parsePattern6(String line) {

		String errorCode=null;
		String errorMessage=null;

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.lastIndexOf(","));

		if(ruleKey.contains(","))
			ruleKey=ruleKey.substring(0,ruleKey.indexOf(","));
		errorCode =keyValMap.get(getStrippedString(ruleKey));

		String errorMessageKey =line.substring(line.lastIndexOf(errorMessageConstantsConst)+errorMessageConstantsConst.length(),line.lastIndexOf("));"));

		if(errorMessageKey.contains(","))
			errorMessageKey=errorMessageKey.substring(0,errorMessageKey.lastIndexOf(","));
		errorMessage =keyValMap.get(getStrippedString(errorMessageKey));

		return new String[] {errorCode+","+errorMessageKey,errorMessage};
	}

	private String[] parsePattern7(String line) {

		String errorCode=null;
		String errorMessage=null;

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.lastIndexOf(","));
		errorCode =keyValMap.get(getStrippedString(ruleKey));

		String errorMessageKey =line.substring(line.indexOf(errorConstantsConst)+errorConstantsConst.length(),line.lastIndexOf("));"));
		errorMessage=keyValMap.get(errorMessageKey);

		return new String[] {errorCode+","+errorMessageKey,errorMessage};
	}

	private String[] parsePattern8(String line) {

		String errorCode=null;
		String errorMessage=null;

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.indexOf(","));
		errorCode =keyValMap.get(getStrippedString(ruleKey));

		String errorMessageKey =line.substring(line.indexOf(fbdaConstantConst)+fbdaConstantConst.length(),line.indexOf("));"));
		errorMessage=keyValMap.get(errorMessageKey);

		return new String[] {errorCode+","+errorMessageKey,errorMessage};
	}

	private String[] parsePattern9(String line) {

		String errorCode=null;

		String ruleKey =line.substring(line.indexOf(errorCodeConstantsConst)+errorCodeConstantsConst.length(),line.indexOf(", \""));
		errorCode =keyValMap.get(getStrippedString(ruleKey));
		String errorMessageKey =line.substring(line.lastIndexOf(", \"")+", ".length(),line.indexOf("\")"));

		return new String[] {errorCode+", "+noFieldSpecifiedConst,errorMessageKey};
	}

	public static Properties readPropertiesFile(String fileName) throws IOException {

		FileInputStream fileInputStream = new FileInputStream(fileName);

		Properties prop = new Properties();
		prop.load(fileInputStream);

		fileInputStream.close();

		return prop;
	}
}
