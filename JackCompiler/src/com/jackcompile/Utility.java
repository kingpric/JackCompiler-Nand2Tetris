package com.jackcompile;

import java.util.Arrays;
import java.util.List;

public class Utility {

	public static String CreateXMLTag(LexicalElements tagName, String tagText) {

		if (tagText.equals("<"))
			tagText = "&lt;";
		else if (tagText.equals(">"))
			tagText = "&gt;";

		return "<" + tagName + ">" + tagText + "</" + tagName + ">";

	}

	public enum LexicalElements {
		StringConstant, symbol, integerConstant, keyword, identifier
	}

	public static boolean verifyKeyword(String kw) {
		String[] definedKeywords = new String[] { "class", "constructor", "function", "method", "field", "static",
				"var", "int", "char", "boolean", "void", "true", "false", "null", "this", "let", "do", "if", "else",
				"while", "return" };
		List<String> lstKW = Arrays.asList(definedKeywords);
		if (lstKW.contains(kw))
			return true;

		return false;

	}
	
	
}
