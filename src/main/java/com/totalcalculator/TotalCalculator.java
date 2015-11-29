package com.totalcalculator;

import java.io.IOException;

public interface TotalCalculator {	
	String SEPARATOR = ";";
	
	void calculate(String inputFileName, String outputFileName) throws IOException;
}
