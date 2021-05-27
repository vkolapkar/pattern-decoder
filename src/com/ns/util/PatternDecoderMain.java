package com.ns.util;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class PatternDecoderMain {

	private static String sourceFolder = ".\\src\\com\\src\\bl";
	private static String sourceFolderUtil = ".\\src\\com\\src\\util\\";
	private static String error_Message_Declaration = "(.*?)public static final (.*?);";
	private static String error_Code_Declaration = "(.*?)RULE_(.*?)\"";
	private static String RuleToErrorCodeMapping_CONST = "RuleToErrorCodeMapping.";
	private static String ErrorCodeConstants_CONST = "ErrorCodeConstants.";
	private static String ErrorLevel = "ErrorLevel.";
	private static String ErrorLoggingUtility = "ErrorLoggingUtility.populateWarningCollection(";
	private static String ANNOTATION_NOTNULL = "@NotNull(";

	private static Map<String, String> keyValMap = new ConcurrentHashMap<String, String>();

	public static void main(String[] args) {

		PatternDecoderMain obj = new PatternDecoderMain();
		obj.startupTask();

	}

	private void startupTask() {
		// read Config

		readAllFilesAndCreateMap();

		assessFilesForErrorCodeNErrorMsg();

	}

	private void readAllFilesAndCreateMap() {

		try {
			Files.walk(Paths.get(sourceFolderUtil)).filter(path -> !path.toFile().isDirectory()).parallel()
					.forEach(filePath -> readFile(filePath));

		} catch (IOException e) {
			System.out.println("Excetion in load RuleError Code map");
			e.printStackTrace();
		}

	}

	private void readFile(Path path) {

		Map<String, String> code_msg = null;
		try {
			code_msg = Files.lines(path).filter(line -> {
				if (isConstantDeclarationPattern(line) || isErrorCode_EnumPattern(line)) {
					return true;
				} else
					return false;
			})// .peek(line->System.out.println("DEBUG "+line))
					.map(line -> normalize(line))
					.collect(Collectors.toMap(keyVal -> getKey(keyVal[0].strip()), keyVal -> keyVal[1]));

		} catch (IOException e) {

			e.printStackTrace();
		}

		keyValMap.putAll(code_msg);

	}
	private String getKey(String key) {
		//System.out.println("KEY!!! "+key);
		if(key.strip().contains(" ")) {
			String formattedKey  =key.trim().substring(key.lastIndexOf(" ")+1, key.length());
		 //System.out.println("KEY+++ "+formattedKey.length()+" key: "+formattedKey.strip());
			return formattedKey.strip() ;
		}
		//System.out.println("KEY### "+key.length()+" key: "+key);
		return key;
	}
	/**
	 * Read file line and convert it in to key value format and store in global map
	 * e.g. I/P public static final String DATA_VALIDATION_CODE = "8000"; O/P Key:
	 * DATA_VALIDATION_CODE Value : "8000
	 * 
	 * I/P RULE_50 Value : "RLE005000" O/P Key: RULE_51 Value : "RLE005000"
	 * 
	 * @param line
	 * @return
	 */
	private String[] normalize(String line) {
		String keyVal[] = new String[2];
		if (isConstantDeclarationPattern(line)) {
			keyVal = line.strip().replace(";", "").split("=");
		} else if (isErrorCode_EnumPattern(line)) {
			keyVal = line.strip().replace("(", "=").replace("),", "").replace(");", "").split("=");

		}
		return keyVal;
	}

	private boolean isConstantDeclarationPattern(String line) {

		String error_Message_Declaration = "(.*?)public static final (.*?);";
		Pattern pattern = Pattern.compile(error_Message_Declaration);
		return pattern.matcher(line).find();

	}

	private boolean isErrorCode_EnumPattern(String line) {

		Pattern pattern = Pattern.compile(error_Code_Declaration);
		return pattern.matcher(line).find();

	}

	private void assessFilesForErrorCodeNErrorMsg() {
		// readFilesFromSourceFolder

		try {
			Files.walk(Paths.get(sourceFolder))
					.filter(path -> (!path.toFile().isDirectory() && path.toString().endsWith(".java"))).parallel()
					.forEach(filePath -> assessFile(filePath));

		} catch (IOException e) {
			System.out.println("Excetion in load RuleError Code map");
			e.printStackTrace();
		}

	}

	private void assessFile(Path path) {
		System.out.println("########### File " + path.toFile().getName());
		if ("NPIAddRequestView.java".equals(path.toFile().getName())) {
			System.out.println("");
		}
		Map<String, String> code_msg = null;
		try {
			code_msg = Files.lines(path).map(line -> checkForExpectedPattern(line)

			).filter(tokenArray -> Integer.parseInt(tokenArray[0]) > 0).map(line -> errorCodeNMsg(line))
					.collect(Collectors.toMap(keyVal -> keyVal[0], keyVal -> keyVal[1], (existingVal, newVal) -> {
						// System.out.println("DUPLICATE val1 "+val1+" val2 "+val2);
						if (existingVal.contains(newVal))
							return existingVal;
						else
							return existingVal + " \n" + newVal;
					}

					));

			if (!code_msg.isEmpty())
				System.out.println("Code VS Messages");
			code_msg.entrySet()
					.forEach(entry -> System.out.println("Key: " + entry.getKey() + " Value : " + entry.getValue()));

		} catch (IOException e) {

			e.printStackTrace();
		}

	}

	
	//TODO: Patterns=PATTERN1|PATTERN2|PATTERN3
	private String[] checkForExpectedPattern(String line) {
		int patternType = 0;
		if(patternMatcher(line, RuleToErrorCodeMapping_CONST,"&",ErrorCodeConstants_CONST)) {
			//if (line.contains(RuleToErrorCodeMapping_CONST) && line.contains(ErrorCodeConstants_CONST)){
			// Check for Pattern
			//ErrorLoggingUtility.populateWarningCollection(errorCollection, RuleToErrorCodeMapping.RULE_50.getErrorCode(), String.format(ErrorCodeConstants.RULE_50_ERROR_MESSAGE, requestProvider.getMasterProviderId()), ErrorLevel.ORANGE, !StringUtils.isEmpty(requestProvider.getMasterProviderId()) ? Long.valueOf(requestProvider.getMasterProviderId()) : null, null, address,methodName);
			patternType = 1;
		}else if (line.contains(ErrorLoggingUtility) && line.contains(RuleToErrorCodeMapping_CONST)) { 
			//ErrorLoggingUtility.populateWarningCollection(errorCollection, RuleToErrorCodeMapping.RULE_50.getErrorCode(), "Invali xys field", ErrorLevel.ORANGE, !StringUtils.isEmpty(requestProvider.getMasterProviderId()) ? Long.valueOf(requestProvider.getMasterProviderId()) : null, null, address,methodName);
			patternType = 2;
		} else if (line.contains(ANNOTATION_NOTNULL) && line.contains(ErrorCodeConstants_CONST)) {
			

			patternType = 3;
			// @NotNull(message = ErrorCodeConstants.DATA_VALIDATION_CODE +// ErrorCodeConstants.SPLITTER + ErrorCodeConstants.RULE_52_ERROR_MESSAGE)

		}
		return new String[] { patternType + "", line };
	}
	
	private boolean patternMatcher(String line,String regx1,String operator,String regx2) {
		Pattern pattern = Pattern.compile(regx1);
		Pattern pattern1 = Pattern.compile(regx2);
	
		return pattern.matcher(line).find() && pattern1.matcher(line).find();

	}

	/**
	 * As per User pre Defined patterns pick up the respective matching pattern and
	 * part it
	 * 
	 * @param line
	 * @return
	 */
	private String[] errorCodeNMsg(String[] tokenArr) {
		String[] errorCode_MsgArr = new String[2];
		String errorCode = null;
		String errorMessage = null;
		int patternType = Integer.parseInt(tokenArr[0]);
		String line = tokenArr[1];

		switch (patternType) {

		case 1: {

			String rule_key = line.substring(
					line.indexOf(RuleToErrorCodeMapping_CONST) + RuleToErrorCodeMapping_CONST.length(),
					line.indexOf(".getErrorCode()"));
			String errorMessage_key = line.substring(
					line.indexOf(ErrorCodeConstants_CONST) + ErrorCodeConstants_CONST.length(),
					line.indexOf("_ERROR_MESSAGE"));

			errorCode = keyValMap.get(rule_key);
			String errorMessageKey = errorMessage_key.strip() + "_ERROR_MESSAGE";
			errorMessage = keyValMap.get(errorMessageKey);
		}

			break;
		case 2:

			// ErrorLoggingUtility.populateWarningCollection(errorCollection,
			// RuleToErrorCodeMapping.RULE_50.getErrorCode(), "Invali xys field",
			// ErrorLevel.ORANGE,
			// !StringUtils.isEmpty(requestProvider.getMasterProviderId()) ?
			// Long.valueOf(requestProvider.getMasterProviderId()) : null, null,
			// address,methodName);
			String rule_key = line.substring(
					line.indexOf(RuleToErrorCodeMapping_CONST) + RuleToErrorCodeMapping_CONST.length(),
					line.indexOf(".getErrorCode()"));
			errorCode = keyValMap.get(rule_key);

			line = line.substring(line.indexOf(".getErrorCode()") + ".getErrorCode()".length());
			errorMessage = line.substring(line.indexOf(","), line.indexOf("\",")); // ,line.indexOf("_ERROR_MESSAGE"));

			break;
		case 3: {
			String rule_key1 = line.substring(
					line.indexOf(ErrorCodeConstants_CONST) + ErrorCodeConstants_CONST.length(), line.indexOf("+"));
			errorCode = keyValMap.get(rule_key1.strip());
			String errorMessageKey = line.substring(
					line.lastIndexOf(ErrorCodeConstants_CONST) + ErrorCodeConstants_CONST.length(), line.indexOf(")"));
			errorMessage = keyValMap.get(errorMessageKey.strip());
		}

			break;
			

		}

		return new String[] { errorCode, errorMessage };
	}

	private String[] parsePattern(String line) {

		String[] errorCode_MsgArr = new String[2];
		String errorCode = null;
		String errorMessage = null;

		boolean flag = false;
		if (line.contains(RuleToErrorCodeMapping_CONST) && line.contains(ErrorCodeConstants_CONST)) {
			// ErrorLoggingUtility.populateWarningCollection(errorCollection,
			// RuleToErrorCodeMapping.RULE_50.getErrorCode(),
			// String.format(ErrorCodeConstants.RULE_50_ERROR_MESSAGE,
			// requestProvider.getMasterProviderId()), ErrorLevel.ORANGE,
			// !StringUtils.isEmpty(requestProvider.getMasterProviderId()) ?
			// Long.valueOf(requestProvider.getMasterProviderId()) : null, null,
			// address,methodName);

			String rule_key = line.substring(
					line.indexOf(RuleToErrorCodeMapping_CONST) + RuleToErrorCodeMapping_CONST.length(),
					line.indexOf(".getErrorCode()"));
			String errorMessage_key = line.substring(
					line.indexOf(ErrorCodeConstants_CONST) + ErrorCodeConstants_CONST.length(),
					line.indexOf("_ERROR_MESSAGE"));

			errorCode = keyValMap.get(rule_key);
			String errorMessageKey = errorMessage_key.strip() + "_ERROR_MESSAGE";
			errorMessage = keyValMap.get(errorMessageKey);

			return new String[] { errorCode, errorMessage };
		} else if (line.contains(ErrorLoggingUtility) && line.contains(RuleToErrorCodeMapping_CONST)) { // TODO: check
																										// if line
																										// contains
																										// ErrorLoggingUtility.populateWarningCollection(errorCollection,
																										// RuleToErrorCodeMapping.
			// ErrorLoggingUtility.populateWarningCollection(errorCollection,
			// RuleToErrorCodeMapping.RULE_50.getErrorCode(), "Invali xys field",
			// ErrorLevel.ORANGE,
			// !StringUtils.isEmpty(requestProvider.getMasterProviderId()) ?
			// Long.valueOf(requestProvider.getMasterProviderId()) : null, null,
			// address,methodName);
			String rule_key = line.substring(
					line.indexOf(RuleToErrorCodeMapping_CONST) + RuleToErrorCodeMapping_CONST.length(),
					line.indexOf(".getErrorCode()"));
			errorCode = keyValMap.get(rule_key);

			line = line.substring(line.indexOf(".getErrorCode()") + ".getErrorCode()".length());
			errorMessage = line.substring(line.indexOf(","), line.indexOf("\",")); // ,line.indexOf("_ERROR_MESSAGE"));
			return new String[] { errorCode, errorMessage.strip() };

		} else if (line.contains(ANNOTATION_NOTNULL) && line.contains(ErrorCodeConstants_CONST)) {// TODO: check if line
																									// has
																									// //@NotNull(message
																									// =
																									// ErrorCodeConstants.
																									// and 3 occurrences
																									// of
																									// ErrorCodeConstants.
			flag = true;

			// @NotNull(message = ErrorCodeConstants.DATA_VALIDATION_CODE +
			// ErrorCodeConstants.SPLITTER + ErrorCodeConstants.RULE_52_ERROR_MESSAGE)
			String rule_key = line.substring(line.indexOf(ErrorCodeConstants_CONST) + ErrorCodeConstants_CONST.length(),
					line.indexOf("+"));
			errorCode = keyValMap.get(rule_key.strip());
			String errorMessageKey = line.substring(
					line.lastIndexOf(ErrorCodeConstants_CONST) + ErrorCodeConstants_CONST.length(), line.indexOf(")"));
			errorMessage = keyValMap.get(errorMessageKey.strip());
			return new String[] { errorCode, errorMessage };
		}

		System.out.println("COUD NOT FOUND MATCHING PATTERN.... FOR LINE \n" + line);
		return errorCode_MsgArr;

	}

}
