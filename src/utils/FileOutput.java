package utils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Formatter;


public class FileOutput {
	public FileOutput(final String fileName, boolean bAppend) {
		String dirName = fileName.substring(0, fileName.lastIndexOf('/'));
		File dir = new File(dirName);  
		if (!dir.exists())
			createDirectory(dirName);
        openFile(fileName, bAppend);
    }
	
	public FileOutput(final String fileName) {
		String dirName = fileName.substring(0, fileName.lastIndexOf('/'));
		File dir = new File(dirName);  
		if (!dir.exists())
			createDirectory(dirName);
        openFile(fileName, false);
    }
	
    private void openFile(final String fileName, boolean bAppend) {
        try {
        	if (bAppend)
        		writer = new Formatter(new FileWriter(fileName, true));
        	else
        		writer = new Formatter(fileName);
        } catch (FileNotFoundException ex) {
        	System.err.println(ex.getLocalizedMessage());
        } catch (IOException ex2) {
        	System.err.println(ex2.getLocalizedMessage());
        }
    }

    public final void close() {
        if (writer != null)
        	writer.close();
    }
    
    public Formatter getWriter() {
        if (writer == null)
        	System.err.println("The file writer is not initialised!");
    	return writer;
    }
    
    Formatter writer = null;
    
    private static void createDirectory(final String dirName) {
		try {
			if (!(new File(dirName)).mkdirs())
				System.err.println("Couldn't create directory " + dirName + ".");
		} catch (Exception e) {// Catch exception if any
			System.err.println("Error: " + e.getMessage());
		}
	}
}
