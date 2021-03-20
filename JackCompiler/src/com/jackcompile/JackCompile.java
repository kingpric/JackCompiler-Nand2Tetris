package com.jackcompile;

import java.io.File;
import java.io.IOException;

public class JackCompile {

	public static void main(String[] args) throws Exception {
		// TODO Auto-generated method stub

		args = new String[] { "G:\\Automation Journey\\nand2tetris\\projects\\10 - Copy\\ArrayTest\\Main.jack" };

		File fs = new File(args[0]);
		Tokenizer tokenizer = new Tokenizer(fs);
		System.out.println(tokenizer.tokenize());

		while (tokenizer.hasMoreToken()) {
			System.out.println(tokenizer.getNodeName() + ":\t" + tokenizer.getNodeText());
		}

	}

}
