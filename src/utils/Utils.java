package utils;

import java.io.File;
import java.util.Calendar;
import java.util.GregorianCalendar;

public class Utils {
	static public Calendar fromStringToCalendar(String str) {
		int day = 0, month = 0, year = 0;
		
		if (str.contains("/")) {
			day = Integer.valueOf(str.split("/")[0]);
			month = Integer.valueOf(str.split("/")[1]) - 1;
			year = Integer.valueOf(str.split("/")[2]);
		} else if (str.contains("-")){
			year = Integer.valueOf(str.split("-")[0]);
			month = Integer.valueOf(str.split("-")[1]) - 1;
		day = Integer.valueOf(str.split("-")[2]);
		} else {
			System.err.println("Error: wrong date format: " + str +"!");
			System.exit(1);
		}
		
		return new GregorianCalendar(year, month, day);
	}
	
//	static public String fromDateToString(Date date) {
//		return "";
//	}
	
	
	public static void createDirectory(final String dirName) {
		try {
			boolean success = (new File(dirName)).mkdirs();
			if (success)
				System.out.println("Directory " + dirName + " created.");
			else
				System.out.println("Couldn't create directory " + dirName + ".");
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
	
	public static int drawFromPoisson(double lambda) {
		double L = Math.exp(-lambda);
		  double p = 1.0;
		  int k = 0;

		  do {
		    k++;
		    p *= Math.random();
		  } while (p > L);

		  return k - 1;
	}
	
	public static boolean bFileExists(String fileName) {
		if ((new File(fileName)).exists())
			return true;
		else
			return false;
	}
	
}