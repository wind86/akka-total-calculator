package com.totalcalculator.util;

import java.io.File;
import java.io.IOException;
import java.util.Map;

import org.apache.commons.io.FileUtils;

public class ResultWriter {

	private static final String RESULT_FORMAT =  "%d => %d%s";
	
	public static void writeResults(final String fileName, final Map<Integer,Integer> results) throws IOException {
		final StringBuilder sb = new StringBuilder();
		for (final Map.Entry<Integer, Integer> entry : results.entrySet()) {
			sb.append(String.format(RESULT_FORMAT, entry.getKey(), entry.getValue(), System.lineSeparator()));
		}

		FileUtils.writeStringToFile(new File(fileName), sb.toString());
	}
}
