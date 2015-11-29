package com.totalcalculator;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import com.totalcalculator.util.FileGenerator;
import com.totalcalculator.util.ResultWriter;

public class SimpleTotalCalculator implements TotalCalculator {	

	private final long startTime = System.currentTimeMillis();
	
	public static void main(String[] args) throws IOException {
		final SimpleTotalCalculator totalCalculator = new SimpleTotalCalculator();
		totalCalculator.calculate("demo.txt", "simple-result.txt");
	}
	
	@Override
	public void calculate(final String inputFileName, final String outputFileName) throws IOException {
		final File file = FileGenerator.generate(inputFileName);		
		final Map<Integer, Integer> calculations = calculateData(FileUtils.readLines(file));
		
		ResultWriter.writeResults(outputFileName, calculations);				
	}
		
	private Map<Integer,Integer> calculateData(final List<String> lines) {		
		final Map<Integer, Integer> calculations = new HashMap<>();
		
		String[] data = null;
		Integer id = null;
		Integer value = null;
		
		for (final String line : lines) {
			data = StringUtils.split(line, SEPARATOR);
			id = Integer.valueOf(data[0]);
			value = Integer.valueOf(data[1]);

			Integer calculatedValue = calculations.get(id);
			if (calculatedValue == null) {
				calculations.put(id, value);
				continue;
			}

			calculations.put(id, calculatedValue += value);
		}

		System.out.println("Duration: " + (System.currentTimeMillis() - startTime) + " ms \n");
		
		return calculations;
	}
	
}
