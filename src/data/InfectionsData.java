package data;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Iterator;
import java.util.List;

import data.MovementsData.MovementsDataRecord;
import utils.Utils;
import utils.FileInput;
import utils.FileOutput;

public class InfectionsData {
	public class InfectionsDataRecord {
		public String CPH;
		public String date;
		
		public InfectionsDataRecord(String CPH, String date) {
			this.date = date;
			this.CPH = CPH;
		}
	}
	
	ArrayList<InfectionsDataRecord> data;
	
	public InfectionsData(String fileName) {
		data = new ArrayList<InfectionsDataRecord>();
		FileInput fileInput = new FileInput(fileName, ",|;");
		
		System.out.print("Started reading file with initially infected farms " + fileName + "... ");
        try {
            List<String> nextLine;
            fileInput.readLine(); // Ignore first line.
            while ((nextLine = fileInput.readLine()) != null) {
    			data.add(new InfectionsDataRecord(nextLine.get(0), nextLine.get(1)));
            }
            fileInput.close();
        } catch (ArrayIndexOutOfBoundsException aioobe) {
        	System.err.println(aioobe.getLocalizedMessage());
        }
        System.out.println("Done!");
	}
	
	public InfectionsData(FarmsData farmsData, MovementsData movementsData) {
		FileOutput logFile = new FileOutput("output/indexCases.log");

		data = new ArrayList<InfectionsDataRecord>();
		
		for (Iterator<String> i = movementsData.getMovementsIterator(); i.hasNext(); ) {
			String from = null;
			String key = (String) i.next();
			MovementsDataRecord record = movementsData.getRecord(key);			

			if (farmsData.getRecord(record.rcpCPH) == null 	 	// recipient is not in Scotland
//					||
//					farmsData.getRecord(record.srcCPH) != null		// source is in Scotland
			)
				continue;	

			if (record.mktCPH.isEmpty()) {
				from = "Directly from farm";
				continue; // consider only movements through markets
			} else  //if (farmsData.getRecord(record.mktCPH) == null)
				from = "Market";
			
			data.add(new InfectionsDataRecord(record.srcCPH, record.date));
			
			if (logFile != null)
				logFile.getWriter().format("%s,%s,%s,%s\n", record.date, record.rcpCPH, from, record.srcCPH);
//				logFile.getWriter().format("%s\n", record.toString());
//			count++;
			
			System.out.print(".");
		}
		logFile.close();
	}

	public ArrayList<String> getInfectedOnDatePremises(Calendar cal) {
		ArrayList<String> cphs = new ArrayList<String>();
		for (InfectionsDataRecord record : data) {
			if (cal.compareTo(Utils.fromStringToCalendar(record.date)) == 0)
				cphs.add(record.CPH);
		}
		return cphs;
	}
	
	public ArrayList<InfectionsDataRecord> getData() {
		return data;
	}
	
	public void writeToFile(String fileName) {
		FileOutput outputFile = new FileOutput(fileName);
		
		outputFile.getWriter().format("CPH,date\n");
		
		for (InfectionsDataRecord record : data) {
			outputFile.getWriter().format("%s,%s\n", record.CPH, record.date);
		}
		outputFile.close();
	}
}
