package com.jackcompile;

import com.jackcompile.Utility.JackStructure;
import com.jackcompile.Utility.LexicalElements;

public class CompileEngine {

	Tokenizer tokenizer;

	public CompileEngine(Tokenizer tokenizer) throws Exception {
		this.tokenizer = tokenizer;
		tokenizer.tokenize();
	}

	private boolean validateNode(LexicalElements nodeName) throws Exception {
		if (!tokenizer.getNodeName().equals(nodeName))
			throw new Exception("Invalid " + nodeName);

		return true;
	}

	public String compile() throws Exception {

		String xmlCompiled = "";
		while (tokenizer.hasMoreToken()) {
			if (!tokenizer.getNodeText().equals("class"))
				throw new Exception("Invalid class");

			// class "className" {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

			tokenizer.advance();
			validateNode(LexicalElements.identifier);
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

			tokenizer.advance();
			validateNode(LexicalElements.symbol);
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

			// classVarDec* subroutineDec*
			while (tokenizer.hasMoreToken()) {

				// (static | field) type varName (,varName)* ;
				if (tokenizer.getNodeText().equals("static") || tokenizer.getNodeText().equals("field")) {
					xmlCompiled += Utility.CreateXMLTag(JackStructure.classVarDec, compileVarDec());
				}

				else if (tokenizer.getNodeText().equals("constructor") || tokenizer.getNodeText().equals("function")
						|| tokenizer.getNodeText().equals("method")) {
					xmlCompiled += compileSubroutine();
				}

			}

			// tokenizer.advance();
			validateNode(LexicalElements.symbol);
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		}
		xmlCompiled = Utility.CreateXMLTag(JackStructure.Class, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles variable declaration
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileVarDec() throws Exception {
		String xmlCompiled = "";

		validateNode(LexicalElements.keyword);

		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		while (tokenizer.hasMoreToken()) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			if (tokenizer.getNodeText().equals(";"))
				break;
		}
		return xmlCompiled;
	}

	/**
	 * Compiles subroutine such as function/method/constructor
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileSubroutine() throws Exception {
		String xmlCompiled = "";

		validateNode(LexicalElements.keyword);

		// (constructor | function | method) (void | "type") "subroutineName
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		// int i = 0; // # of simultaneous compile
		while (tokenizer.hasMoreToken() && !tokenizer.getNodeText().equals("(")) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		}

		// compile parameter list
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		xmlCompiled += compileParameterList();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		// compile subroutine body
		xmlCompiled += compileSubroutineBody();

		xmlCompiled = Utility.CreateXMLTag(JackStructure.subroutineDec, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles parameter list <b>type <i>varName</i> (, type <i>varName</i>)*?</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileParameterList() throws Exception {

		String xmlCompiled = "";

		while (tokenizer.hasMoreToken() && !tokenizer.getNodeText().equalsIgnoreCase(")")) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		}

		xmlCompiled = Utility.CreateXMLTag(JackStructure.parameterList, xmlCompiled);
		return xmlCompiled;

	}

	/**
	 * Compiles subroutine body <b>{ <i>varDec* statements<i>}</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileSubroutineBody() throws Exception {
		String xmlCompiled = "";

		tokenizer.advance();
		if (!tokenizer.getNodeText().equalsIgnoreCase("{"))
			throw new Exception("Invalid block declaration");

		// "{"
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		while (tokenizer.hasMoreToken() && !tokenizer.getNodeText().equals("}")) {

			// local variable declaration
			if (tokenizer.getNodeText().equals("var")) {
				xmlCompiled += Utility.CreateXMLTag(JackStructure.varDec, compileVarDec());
			}

			// statements
			else if (tokenizer.getNodeText().equals("let") || tokenizer.getNodeText().equals("if")
					|| tokenizer.getNodeText().equals("while") || tokenizer.getNodeText().equals("do")
					|| tokenizer.getNodeText().equals("return")) {
				xmlCompiled += compileStatements();
			}
		}

		// "}"
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		xmlCompiled = Utility.CreateXMLTag(JackStructure.subroutineBody, xmlCompiled);

		return xmlCompiled;
	}

	/**
	 * Compiles statements
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileStatements() throws Exception {
		String xmlCompiled = "";

		tokenizer.stepBack();
		while (tokenizer.hasMoreToken() && (tokenizer.getNodeText().equals("let")
				|| tokenizer.getNodeText().equals("if") || tokenizer.getNodeText().equals("while")
				|| tokenizer.getNodeText().equals("do") || tokenizer.getNodeText().equals("return"))) {

			switch (tokenizer.getNodeText().toString()) {

			case "let":
				xmlCompiled += compileLetStatement();
				break;

			case "if":
				xmlCompiled += compileIfStatement();
				break;

			case "do":
				xmlCompiled += compileDoStatement();
				break;

			case "while":
				xmlCompiled += compileWhileStatement();
				break;

			case "return":
				xmlCompiled += compileReturnStatement();
				break;

			}
		}
		tokenizer.stepBack();

		xmlCompiled = Utility.CreateXMLTag(JackStructure.statements, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles expression <b>term (op terms)*</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileExpression() throws Exception {
		String xmlCompiled = "";

		xmlCompiled = compileTerm();

		// if (xmlCompiled == "")
		// throw new Exception("Invalid term expression");

		while (tokenizer.hasMoreToken() && Utility.isOpKeyword(tokenizer.getNodeText())) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			xmlCompiled += compileTerm();
		}

		tokenizer.stepBack();
		xmlCompiled = Utility.CreateXMLTag(JackStructure.expression, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles expression list <b>(expression (, expression)*)?</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileExpressionList() throws Exception {
		String xmlCompiled = "";

		// xmlCompiled = compileExpression();
		while (tokenizer.hasMoreToken() && !tokenizer.getNodeText().equals(")")) {
			if (tokenizer.getNodeText().equals(","))
				xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			else
				tokenizer.stepBack();
			
			xmlCompiled += compileExpression();

		}

		tokenizer.stepBack();

		xmlCompiled = Utility.CreateXMLTag(JackStructure.expressionList, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles term expression <b>integerConstant | stringConstant |
	 * keywordConstant | <i>varName</i> | <i>varName[ expression ]</i> |
	 * subroutineCall | <i>(expression)</i> | unaryOp term</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileTerm() throws Exception {
		String xmlCompiled = "";

		if (tokenizer.hasMoreToken()) {

			// integerConstant | stringConstant | keywordConstant
			if (tokenizer.getNodeName() == LexicalElements.integerConstant
					|| tokenizer.getNodeName() == LexicalElements.stringConstant
					|| Utility.isKeywordConstants(tokenizer.getNodeText())) {
				xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

			}

			// unaryOp term
			else if (Utility.isUnaryOpKeywords(tokenizer.getNodeText())) {
				xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
				xmlCompiled += compileTerm();
			}

			// (expression)
			else if (tokenizer.getNodeText().equals("(")) {
				xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
				xmlCompiled += compileExpression();
				tokenizer.advance();
				xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			}

			// varName | varName[expression] | subroutineCall
			else {
				String xmlCompiledTemp = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
				tokenizer.advance();

				// [expression]
				if (tokenizer.getNodeText().equals("[")) {
					xmlCompiled = xmlCompiledTemp;
					xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
					xmlCompiled += compileExpression();
					tokenizer.advance();
					xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
				}

				//
				else if (tokenizer.getNodeText().equals("(") || tokenizer.getNodeText().equals(".")) {
					tokenizer.stepBack(2);
					xmlCompiled = compileSubroutineCall();

				} else {
					xmlCompiled = xmlCompiledTemp;
					tokenizer.stepBack();
				}
			}

		}
		xmlCompiled = Utility.CreateXMLTag(JackStructure.term, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles subroutine call <b> <i>subroutineName</i> (expressionList) |
	 * (<i>className</i> | <i>varName</i>).<i>subroutineName</i>(expressionList)
	 * </b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileSubroutineCall() throws Exception {
		String xmlCompiled = "";

		tokenizer.advance();
		xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();

		if (tokenizer.getNodeText().equals("(")) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			xmlCompiled += compileExpressionList();
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		}

		else if (tokenizer.getNodeText().equals(".")) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			xmlCompiled += compileExpressionList();
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		}

		return xmlCompiled;
	}

	/**
	 * Compiles let statement body <b>let <i>varName([expressions])?</i> =
	 * expressions ; </b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileLetStatement() throws Exception {
		String xmlCompiled = "";

		// let varName ([expression])? = expression ;
		// let varName
		xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();

		// [expression]
		if (tokenizer.getNodeText().equals("[")) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			xmlCompiled += compileExpression();
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			tokenizer.advance();
		}

		// = expression ;
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		xmlCompiled += compileExpression();
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		xmlCompiled = Utility.CreateXMLTag(JackStructure.letStatement, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles if statement <b>if (expression){statements} (else {statements})?</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileIfStatement() throws Exception {
		String xmlCompiled = "";

		// if (expression) { statements }
		xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		// tokenizer.advance();
		xmlCompiled += compileExpression();
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += compileStatements();
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		// (else { statements})?
		tokenizer.advance();
		if (tokenizer.getNodeText().equals("else")) {
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
			tokenizer.advance();
			xmlCompiled += compileStatements();
			tokenizer.advance();
			xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		} else
			tokenizer.stepBack();

		xmlCompiled = Utility.CreateXMLTag(JackStructure.ifStatement, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles while statement <b>while(expression) {statements}</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileWhileStatement() throws Exception {
		String xmlCompiled = "";

		// while (expression) { statements }
		xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		// tokenizer.advance();
		xmlCompiled += compileExpression();
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();
		xmlCompiled += compileStatements();
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		xmlCompiled = Utility.CreateXMLTag(JackStructure.whileStatement, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles return statement <b>return (expression)? ;</b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileReturnStatement() throws Exception {
		String xmlCompiled = "";

		// return
		xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		tokenizer.advance();

		// (expression)?
		if (!tokenizer.getNodeText().equals(";")) {
			tokenizer.stepBack();
			xmlCompiled += compileExpression();
			tokenizer.advance();
		}

		// ;
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());

		xmlCompiled = Utility.CreateXMLTag(JackStructure.returnStatement, xmlCompiled);
		return xmlCompiled;
	}

	/**
	 * Compiles do statement <b>do <i>subroutineCall</i></b>
	 * 
	 * @return compiled string in xml
	 * @throws Exception
	 */
	private String compileDoStatement() throws Exception {
		String xmlCompiled = "";

		xmlCompiled = Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		xmlCompiled += compileSubroutineCall();
		tokenizer.advance();
		xmlCompiled += Utility.CreateXMLTag(tokenizer.getNodeName(), tokenizer.getNodeText());
		xmlCompiled = Utility.CreateXMLTag(JackStructure.doStatement, xmlCompiled);

		return xmlCompiled;
	}

}
