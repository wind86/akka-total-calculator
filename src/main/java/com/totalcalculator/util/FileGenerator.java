package com.totalcalculator.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

public class FileGenerator {

	private static final int SIZE = 1000;
	private static final int IRETATIONS = 100;
	
	private static final String DATA_FORMAT = "%d;%d\n";
	
	public static void main(String[] args) throws IOException {
		FileGenerator.generate("demo.txt");
	}
	
	public static File generate(String fileName) throws IOException {
		final File file = new File(fileName);
		if (!file.exists()) {
			file.createNewFile();
		}
		
		populateData(file, generateData());

		System.out.println(file.getAbsolutePath());
		return file;
	}
	
	private static Map<Integer, Integer> generateData() {
		final Map<Integer, Integer> data = new HashMap<>();
		for (int i = 1; i <= SIZE; i++) {
			data.put(i, i % 2 == 0 ? 1 : 2);
		}
		return data;
	}
	
	private static void populateData(final File file, final Map<Integer,Integer> data) throws IOException {
		BufferedWriter bw = null;
		try {
			bw = new BufferedWriter(new FileWriter(file.getAbsoluteFile()));
			
			for (int i = 0; i < IRETATIONS; i++) {
				for (int j = 1; j <= SIZE; j++) {
					bw.write(String.format(DATA_FORMAT, j, data.get(j)));
				}
			}
		} finally {
			IOUtils.closeQuietly(bw);
		}
	}
}
