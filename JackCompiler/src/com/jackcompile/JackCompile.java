package com.jackcompile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class JackCompile {

	public static void main(String[] args) throws Exception {

		// args = new String[] { "G:\\Automation Journey\\nand2tetris\\projects\\10 - Copy" };
		// args = new String[] { "G:\\Automation Journey\\nand2tetris\\projects\\10 - Copy\\Square\\Square.jack" };

		for (int i = 0; i < args.length; i++) {

			String checkfilepath = args[i];
			File checkfile = new File(checkfilepath);
			boolean isCheckFile = checkfile.isFile();
			boolean isCheckDirectory = checkfile.isDirectory();

			List<String> filesList = new ArrayList<String>();

			if (isCheckDirectory) {

				Files.walk(Paths.get(checkfilepath)).filter(Files::isRegularFile)
						.filter(f -> f.toAbsolutePath().toString().endsWith(".jack"))
						.forEach(x -> filesList.add(x.toAbsolutePath().toString()));

			} else if (isCheckFile && checkfile.getName().endsWith(".jack")) {

				filesList.add(args[i]);
			} else
				continue;

			for (String filepath : filesList) {

				System.out.println("Compiling.. " + filepath);
				
				File file = new File(filepath);
				String outFileName = file.getParent() + "\\"
						+ file.getName().substring(0, file.getName().length() - 5);

				File fsTout = new File(outFileName + ".T.interim.xml");
				File fsout = new File(outFileName + ".interim.xml");

				if (fsout.exists())
					fsout.delete();

				if (fsTout.exists())
					fsTout.delete();

				BufferedWriter bfwrite = new BufferedWriter(new FileWriter(fsout));
				BufferedWriter bfwriteT = new BufferedWriter(new FileWriter(fsTout));

				try {
					Constants.FileName = file.getName().substring(0, file.getName().length() - 3);

					Tokenizer tokenizer = new Tokenizer(file);
					bfwriteT.append(tokenizer.tokenize().replaceAll("><", ">\n<"));

					CompileEngine engine = new CompileEngine(tokenizer);
					String xmlOut = engine.compile();
					bfwrite.append(xmlOut.replaceAll("><", ">\n<"));

				} finally {
					bfwrite.flush();
					bfwrite.close();

					bfwriteT.flush();
					bfwriteT.close();

				}

			}

		}

	}

}
