package com.jackcompile;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.jackcompile.Utility.LexicalElements;

public class Tokenizer {

	private File jsFile;
	private StringBuilder xmlBuilder = new StringBuilder();
	private NodeList ndList;
	private int docIndex = 0;
	private String nodeName = null;
	private String nodeText = null;

	public Tokenizer(File inputFile) throws ParserConfigurationException {
		jsFile = inputFile;
	}

	public String tokenize() throws Exception {

		FileReader fRead = new FileReader(jsFile);
		BufferedReader bufRead = new BufferedReader(fRead);

		try {

			bufRead.mark(1);
			int charset = -1;
			int preCharset = bufRead.read();

			if (preCharset == -1)
				return "";

			xmlBuilder.append("<tokens>");
			bufRead.reset();

			// boolean strLiteral = false;

			// looping till the EOF
			while (true) {

				charset = bufRead.read();
				bufRead.mark(1);

				// Checking EOF
				if (charset == -1)
					break;

				// char ch = (char) charset;
				// System.out.println(charset + ":" + ch);

				// Checking for string literals
				if (charset == 34) {
					String token = "";
					while (true) {
						int advCharset = bufRead.read();

						// checking if string containing escape character
						if (charset == 92 && (advCharset == 92 || advCharset == 34 || advCharset == 39)) {
							token += (char) advCharset;
						} else if (charset == 92 && (advCharset == 98 || advCharset == 116 || advCharset == 110
								|| advCharset == 114 || advCharset == 102)) {
							token += (char) charset;
							token += (char) advCharset;
						} else if (advCharset == 92) {
							throw new Exception("Invalid escape character");
						}

						// End of string liternal
						else if (advCharset == 34) {
							xmlBuilder.append(Utility.CreateXMLTag(LexicalElements.StringConstant, token));
							token = "";
							charset = advCharset;
							break;
						} else {
							token += (char) advCharset;
						}
					}

				}

				// checking and discarding for comments
				else if (charset == 47) {
					bufRead.mark(1);
					int advCharset = bufRead.read();

					// checking and discarding for comments (//)
					if (advCharset == 47) {
						while (bufRead.read() != 10) {
							// discarding all intermediate charset till CRLF
						}
						continue;
					}

					// checking and discarding for comments (/** */)
					else if (advCharset == 42 && bufRead.read() == 42) {
						while (charset != 42 && (advCharset = bufRead.read()) != 47) {
							// discarding all intermediate charset '*/'
							charset = advCharset;
						}
						continue;

					} else {
						bufRead.reset();
						charset = bufRead.read();

					}

				}

				// checking keyword/identifier
				else if ((charset >= 97 && charset <= 122) || charset == 95 || (charset >= 65 && charset <= 90)) {
					String kwToken = String.valueOf((char) charset);
					while (((charset = bufRead.read()) >= 97 && charset <= 122) || (charset >= 48 && charset <= 57)
							|| (charset >= 65 && charset <= 90)) {
						kwToken += String.valueOf((char) charset);
						bufRead.mark(1);
					}
					if (Utility.verifyKeyword(kwToken))
						xmlBuilder.append(Utility.CreateXMLTag(LexicalElements.keyword, kwToken));
					else
						xmlBuilder.append(Utility.CreateXMLTag(LexicalElements.identifier, kwToken));

					bufRead.reset();

				}

				// checking symbol
				else if ((charset >= 33 && charset <= 47) || (charset >= 58 && charset <= 64)
						|| (charset >= 123 && charset <= 126)) {
					xmlBuilder.append(Utility.CreateXMLTag(LexicalElements.symbol, String.valueOf(((char) charset))));
				}

				// checking integer constant
				else if (charset >= 48 && charset <= 57) {

					String intToken = String.valueOf((char) charset);
					while ((charset = bufRead.read()) >= 48 && charset <= 57) {
						intToken += String.valueOf((char) charset);
						bufRead.mark(1);
					}

					xmlBuilder.append(Utility.CreateXMLTag(LexicalElements.integerConstant, intToken));
					bufRead.reset();
				}

				preCharset = charset;

			}
		} finally

		{
			bufRead.close();
		}
		xmlBuilder.append("</tokens>");

		ByteArrayInputStream input = new ByteArrayInputStream(xmlBuilder.toString().getBytes("UTF-8"));

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		DocumentBuilder builder = factory.newDocumentBuilder();
		Document document = builder.parse(input);
		document.getDocumentElement().normalize();
		ndList = document.getDocumentElement().getChildNodes();

		return xmlBuilder.toString();
	}

	public String advance() {

		nodeName = ndList.item(docIndex).getNodeName();
		nodeText = ndList.item(docIndex).getTextContent();
		
		docIndex++;
		return nodeName;

	}

	public String getNodeName() {
		return nodeName;
	}

	public String getNodeText() {
		return nodeText;
	}

	public boolean hasMoreToken() {
		if (ndList.getLength() == docIndex)
			return false;
		
		advance();
		return true;
	}

}
