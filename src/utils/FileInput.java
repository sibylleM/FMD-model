package utils;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.LinkedList;

/**
 * Simple interface for reading from a file. It simply wraps the java io classes
 * to simplify the input of data.
 * @author Anthony O'Hare
 */
public class FileInput {

    /**
     * Open a handle to a file with the given name.
     * @param dataFileName the name of the file.
     */
    public FileInput(final String dataFileName) {
        fieldSep = DEFAULT_SEP;
        openFile(dataFileName);
    }

    /** Construct a CSV parser with a given separator.
     * @param dataFileName the name of the file.
     * @param sep The single char for the separator (not a list of
     * separator characters)
     */
    public FileInput(final String dataFileName, final String sep) {
        fieldSep = sep;
        openFile(dataFileName);
    }

    /**
     * Read a line from the input file, split it according to the separator specified in the constructor and get a list of
     * tokens from the line. Comment characters (#) are supported.
     * @return a list of entries in the line.
     */
    public final List<String> readLine() {
        List<String> tokens = null;

        try {
            String line = reader.readLine();
            while (line != null && line.charAt(0) == '#') {
                 line = reader.readLine();
            }

            if (line != null) {
                tokens = new  LinkedList<String>();
                String[] split = line.split(fieldSep);
                for (String token : split) {
                    if (token.indexOf("#") < 0) {
                        tokens.add(token);
                    } else {
                        tokens.add(token.substring(0, token.indexOf("#")).trim());
                    }
                }
            }

        } catch (IOException ex) {
            System.err.println(ex.getLocalizedMessage());
        }

        return tokens;
    }

    /**
     * Open the file specified.
     * @param dataFileName the file we will open.
     */
    private void openFile(final String dataFileName) {
        try {
            reader = new BufferedReader(new FileReader(dataFileName));
        } catch (FileNotFoundException ex) {
        	System.err.println(ex.getLocalizedMessage());
        }
    }

    /**
     * Closes the file stream.
     */
    public final void close() {
        try {
            reader.close();
        } catch (IOException ex) {
        	System.err.println(ex.getLocalizedMessage());
        }
    }
    private BufferedReader reader;
    private static final String DEFAULT_SEP = ",";
    private String fieldSep;
}

