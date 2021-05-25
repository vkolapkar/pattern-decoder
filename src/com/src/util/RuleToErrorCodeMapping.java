package com.src.util;

public class RuleToErrorCodeMapping {
	
	
	public enum ENUMVALUES {
	
		RULE_50("RLE005000"),
		RULE_51("RLE005001");
		
		String value;
		private ENUMVALUES(String value){
			this.value=value;
		}
		
		public String getValue() {
			return value;
		}
	}

}
