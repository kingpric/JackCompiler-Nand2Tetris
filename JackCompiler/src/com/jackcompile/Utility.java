package com.jackcompile;

import java.util.Arrays;
import java.util.List;

public class Utility {

	public static String CreateXMLTag(LexicalElements tagName, String tagText) {

		if (tagText.equals("<"))
			tagText = "&lt;";
		else if (tagText.equals(">"))
			tagText = "&gt;";
		else if (tagText.equals("&"))
			tagText = "&amp;";

		return "<" + tagName + "> " + tagText + " </" + tagName + ">";

	}

	public static String CreateXMLTag(JackStructure tagName, String tagText) {

		if (tagText.equals("<"))
			tagText = "&lt;";
		else if (tagText.equals(">"))
			tagText = "&gt;";
		else if (tagText.equals("&"))
			tagText = "&amp;";

		return "<" + tagName + ">" + tagText + "</" + tagName + ">";

	}

	public enum LexicalElements {
		stringConstant, symbol, integerConstant, keyword, identifier;

	}

	public enum JackStructure {
		Class, className, subroutineDec, subroutineBody, varDec, statements, letStatement, expression, term,
		expressionList, whileStatement, doStatement, ifStatement, returnStatement, classVarDec, parameterList;

		@Override
		public String toString() {
			return String.valueOf(this.name().charAt(0)).toLowerCase() + this.name().substring(1);
		}
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

	public enum VarDecKeywords {
		Static, field
	}

	public static boolean isOpKeyword(String key) {
		for (OpKeywords value : OpKeywords.values()) {
			if (value.toString().equals(key))
				return true;
		}

		return false;
	}

	public enum OpKeywords {
		PLUS("+"), Minus("-"), Product("*"), Divide("/"), And("&"), Or("|"), LT("<"), GT(">"), Equals("=");

		String symbol;

		private OpKeywords(String symbol) {
			this.symbol = symbol;
		}

		@Override
		public String toString() {
			return this.symbol;
		}

	}

	public static boolean isUnaryOpKeywords(String key) {
		for (UnaryOpKeywords value : UnaryOpKeywords.values()) {
			if (value.toString().equals(key))
				return true;
		}

		return false;
	}

	public enum UnaryOpKeywords {
		Minus("-"), Not("~");

		String symbol;

		private UnaryOpKeywords(String symbol) {
			this.symbol = symbol;
		}

		@Override
		public String toString() {
			return this.symbol;
		}

	}

//	public static <E extends Enum<E>> boolean EnumContainValue(Class<E> enumType, String key) {
//
//		for (E value : enumType.getEnumConstants()) {
//			if (value.toString().equals(key))
//				return true;
//		}
//
//		return false;
//	}

	public static boolean isKeywordConstants(String key) {
		for (KeywordConstants value : KeywordConstants.values()) {
			if (value.toString().equals(key))
				return true;
		}

		return false;
	}

	public enum KeywordConstants {
		TRUE("true"), FALSE("false"), NULL("null"), THIS("this");

		String symbol;

		private KeywordConstants(String symbol) {
			this.symbol = symbol;
		}

		@Override
		public String toString() {
			return this.symbol;
		}

	}

}
