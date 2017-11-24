/*
 * To the extent possible under law, the ImageJ developers have waived
 * all copyright and related or neighboring rights to this tutorial code.
 *
 * See the CC0 1.0 Universal license for details:
 *     http://creativecommons.org/publicdomain/zero/1.0/
 */

package de.mpicbg.tds;

import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Map;
import java.util.Properties;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashMap;


import org.scijava.command.Command;
import org.scijava.plugin.Plugin;

import ij.IJ;
import ij.macro.ExtensionDescriptor;
import ij.macro.Functions;
import ij.macro.MacroExtension;
import net.imagej.ImageJ;



/**
 * This example illustrates how to create an ImageJ {@link Command} plugin.
 * <p>
 * The code here is a simple Gaussian blur using ImageJ Ops.
 * </p>
 * <p>
 * You should replace the parameter fields with your own inputs and outputs,
 * and replace the {@link run} method implementation with your own logic.
 * </p>
 */
@Plugin(type = Command.class, menuPath = "Plugins>Fiji-Tools-for-HCS>Development>Fiji-Tools-for-HCS-plugin")  
public class FijiToolsForHCSplugin implements MacroExtension, Command {

	private ExtensionDescriptor[] extensions = {
			ExtensionDescriptor.newDescriptor("getNumberToString", this, ARG_OUTPUT + ARG_NUMBER, ARG_NUMBER, ARG_NUMBER), 
			ExtensionDescriptor.newDescriptor("saveLog", this, ARG_STRING), 
			ExtensionDescriptor.newDescriptor("getRegexMatchesFromArray", this, ARG_ARRAY, ARG_STRING, ARG_OUTPUT + ARG_STRING), // oldName
			ExtensionDescriptor.newDescriptor("getMacroExtensionVersion", this),
			ExtensionDescriptor.newDescriptor("getMacroExtensionNames", this),
			ExtensionDescriptor.newDescriptor("testDoubleArray", this, ARG_OUTPUT + ARG_STRING, ARG_ARRAY), 
			ExtensionDescriptor.newDescriptor("testStringArray", this, ARG_OUTPUT + ARG_STRING, ARG_ARRAY)
	};
	
	@Override
	public ExtensionDescriptor[] getExtensionFunctions() {
		// TODO Auto-generated method stub
		//System.out.println("done");
		return extensions;
	}

	@Override
	public String handleExtension(String name, Object[] args) {
		// TODO Auto-generated method stub
		IJ.log("MacroExtension function was called: " + name);
		if (name.equals("getNumberToString")) {
			Double number = ((Double[]) args[0])[0];  // since this is first argument (or/and output argument) this is an array (array of length 1 by definition => see documentation) 
			Integer decimalPlaces = ((Double) args[1]).intValue();
			Integer lengthNumberString = ((Double) args[2]).intValue();
			
			String newNumber = getNumberToString(number, decimalPlaces, lengthNumberString);

			// return result as string array
			String[] returnStringArray = {newNumber};
			args[0] = returnStringArray;   // String[] args[0] = newNumber; doesnt work because Double <-> String entire 1 dim array must be replaced 		
		} 
		if (name.equals("saveLog")) {
			String logPath = (String) args[0];  // since this is first argument (or/and output argument) this is an array (array of length 1 by definition => see documentation)

			saveLogFunction(logPath);
		} 
		if (name.equals("getRegexMatchesFromArray")) {
			String[] stringArrayForQuery = getStringArrayFromObject((Object[]) args[0]);
			String regexPattern = (String) args[1]; 
			String[] resultString = new String[1];                                                  // resultString contains all regex matches as concatenated String and is given back to IJ-macro a 1-element String[] array		
			
			//String[] stringArrayForQuery = {"171020-dyes-10x_G03_T0001F005L01A01Z01C01.tif","171020-dyes-10x_G04_T0001F004L01A01Z01C02.tif","171020-dyes-10x_H05_T0001F003L01A01Z01C04.tif"}; 
			//String regexPattern = "(.*)_([A-P][0-9]{2})_(T[0-9]{4})(F[0-9]{3})(L[0-9]{2})(A[0-9]{2})(Z[0-9]{2})(C[0-9]{2}).tif$";
			//regexPattern = "(?<barcode>.*)_(?<well>[A-P][0-9]{2})_(?<timePoint>T[0-9]{4})(?<field>F[0-9]{3})(?<timeLine>L[0-9]{2})(?<action>A[0-9]{2})(?<plane>Z[0-9]{2})(?<channel>C[0-9]{2}).tif$";
			
			resultString[0] = getStringFromHashMap(getRegexHashMap(stringArrayForQuery, regexPattern), "||", "\t");     // here the HashSets are delimited by the first String parameter and the HashValues by the second
			//IJ.log("IJ: End of code " + resultString[0]);                                                             // for debugging
			
			// return result as string array of length 1
			args[2] = resultString;   
		}
		if (name.equals("getMacroExtensionVersion")) {
			IJ.log("MacroExtension Fiji-Tools-for-HCS-plugin version: " + FijiToolsForHCSplugin.class.getPackage().getImplementationVersion());
		}
		if (name.equals("getMacroExtensionNames")) {
			IJ.log("MacroExtension Fiji-Tools-for-HCS-plugin contains " + extensions.length + " functions:");
			for (int i = 0; i < extensions.length; ++i) {
				IJ.log("Ext." + extensions[i].name + "();       // function needs " + extensions[i].argTypes.length + " parameters");
			}
		}
		if (name.equals("testStringArray")) {  // this is just a test code for handling string arrays
			IJ.log("IJ: before cast ARRAY: " + ((Object[]) args[1])[0].getClass().getName());
			IJ.log("IJ: before cast ARRAY: " + ((Object[]) args[1])[0].toString());
			String myString = ((String) ((Object[]) args[1])[0]); // since args[1] is an object[] in an object[] it has to be cast first to an object[], then to string[]
			IJ.log("IJ: after cast: " + myString);

			// cast has to be done element-by=element and code above is wrapped into this 'get'-function
			String[] stringArrayFromFunction = getStringArrayFromObject((Object[]) args[1]); 
			IJ.log("IJ: stringArrayFromFunction:  " + stringArrayFromFunction[0]);	
			
			// array cannot be passed back, therefore all array values have to be concatenated into one string and are passed back as first args element
			((String[]) args[0])[0] = getStringFromArray(stringArrayFromFunction, "\t");  
		}
		if (name.equals("testDoubleArray")) {   // this is just a test code for handling doulbe arrays
			IJ.log("IJ: before cast ARRAY: " + ((Object[]) args[1])[0].getClass().getName());
			IJ.log("IJ: before cast ARRAY: " + ((Object[]) args[1])[0].toString());
			//IJ.log("IJ: object is " + args.length + " " + args[0].getClass().getName());
			//IJ.log("IJ: object is " + args.length + " " + args[1].getClass().getName());
			
			// cast has to be done element-by=element and this is wrapped into this 'get'-function
			Double[] doubleArrayFromFunction = getDoubleArrayFromObject((Object[]) args[1]); 
			IJ.log("IJ: doubleArrayFromFunction:  " + doubleArrayFromFunction[0]);	
			
			// turn double values into string values 
			String[] doubleArrayAsString = new String[doubleArrayFromFunction.length];
			for (int i = 0; i < doubleArrayFromFunction.length; i++) {
				doubleArrayAsString[i] = doubleArrayFromFunction[i].toString();
			}

			// array cannot be passed back, therefore all array values have to be concatenated into one string and are passed back as first args element
			((String[]) args[0])[0] = getStringFromArray(doubleArrayAsString, "\t");  
		} 
		return null;
	}
	
	//function returns a number in specific string format, e.g 2.5 => 02.500, example: myStringNumber = getNumberToString(2.5, 3, 6);
	public static String getNumberToString(Double number , int decimalPlaces, int lengthNumberString) { 
		String numberString = new String();
		numberString = "000000000000" + String.format("%." + decimalPlaces + "f", number);                            //convert to number to string and add zeros in the front
		numberString = numberString.substring(numberString.length() - lengthNumberString, numberString.length());     //shorten string to lengthNumberString
		//IJ.log("IJ: final string number in method " + numberString);                                                // for debugging
		return numberString;
	}  

	//function saves the log window of ImageJ @ the given path, example: saveLog("C:\\Temp\\Log_temp.txt");
	public static void saveLogFunction(String logPath) { 
		try (FileWriter out = new FileWriter (logPath)) {
			IJ.saveAs("Text", logPath + "2"); //save Log window
		} catch  (IOException e) {
			IJ.log("IJ: couldnot save log file: " + logPath);
			e.printStackTrace();
		}
	}
	
	// this function finds all unique matches from a group-named regex applied on an array of strings[] (e.g. file names) and gives back a LinkedHashMap, where the keys are the group names of the regex and the values are HashSets containing the unique values found for the group  
	private static LinkedHashMap<String, HashSet> getRegexHashMap(String[] searchStrings, String regexPattern) {
		// Create a Pattern object and get all named groups
		Pattern r = Pattern.compile(regexPattern);
		String[] namedGroups = getNamedGroupCandidates(regexPattern);
		
		// Create a LinkedHashMap of HashSets, where the key of the Map is the group name (e.g. well) and the value of the Map is the HashSet (e.g. collection of unique wells like C03, C05, D08) 
		LinkedHashMap<String, HashSet> uniqueRegexValues = new LinkedHashMap<String, HashSet>();

		// Fill the HashMap with group names and empty HashSets
		for (String regexGroup : namedGroups) {
			uniqueRegexValues.put(regexGroup, new HashSet());
			//System.out.println("inFor " + regexGroup + "\n" + uniqueRegexValues);                                       // for debugging
		}
		//System.out.println("FinallyafterInit\n" + uniqueRegexValues);                                                   // for debugging
		
		// Now iterate over array of strings and create matcher object for each one of it...
		for (int i=0; i < searchStrings.length; i++) {                                                                    // i = iteration variable for query string array 
			Matcher m = r.matcher(searchStrings[i]);
			// check if regex found a group, and if yes then put all groups into the HashSet (collection unique values) under the name of the current group
			if (m.find()) {
				for (int j=0; j <= m.groupCount(); j++) {                                                                 // j = iteration variable for groups found in regex matcher 
					//System.out.println("Found value in group" + j + ": " + namedGroups[j] + " is " + m.group(j));       // for debugging
					uniqueRegexValues.get(namedGroups[j]).add(m.group(j));
				}
			} else {
				System.out.println("nothing matched: "  + i + ": " + searchStrings[i]);     // no match found
			}
		}
		//for (String regexGroup : namedGroups) System.out.println(uniqueRegexValues.get(regexGroup));                    // for debugging
		if (uniqueRegexValues.containsKey("queryString")) uniqueRegexValues.remove("queryString");                        // removes the first key-value pair that contains the HashSets of queried strings (= file names!?)
		//System.out.println("end of getRegexHashMap: size = " + uniqueRegexValues.size() + "\n" + uniqueRegexValues);    // for debugging
		return uniqueRegexValues;
	}

//  =================================== H E L P E R  - F U N C T I O N S ===========================================
	
	// this function casts an object array (object[]) which is array from IJ-macro into a string array 
	private static String[] getStringArrayFromObject(Object[] objectArray) { 
		String[] stringArray = new String[objectArray.length];  // empty string array
		for (int i = 0; i < objectArray.length; i++) {
			// cast needs to be done element by element
			stringArray[i] = objectArray[i].toString();
			//IJ.log("IJ: in function array element " + i + " is " + stringArray[i]); // for debugging
		}
		return stringArray;
	} 
	
	// this function casts an object array (object[]) which is array from IJ-macro into a double array 
	private static Double[] getDoubleArrayFromObject(Object[] objectArray) { 
		Double[] doubleArray = new Double[objectArray.length];  // empty double array
		for (int i = 0; i < objectArray.length; i++) {
			// cast needs to be done element by element
			doubleArray[i] = (Double) objectArray[i];
			//IJ.log("IJ: in function array element " + i + " is " + doubleArray[i]); // for debugging
		}
		return doubleArray;
	} 
	
	// this function concatenates array elements into a single string using the given delimiter
	private static String getStringFromArray(String[] stringArray, String delimiter) {
		String stringContainingArray = new String();
		if (stringArray.length > 0) {
			stringContainingArray = stringArray[0];   // put first array element in string
		} else {
			stringContainingArray = "";               // array is empty => string is empty
		}
		for (int i = 1; i < stringArray.length; i++) {
			stringContainingArray += delimiter + stringArray[i];
			//IJ.log("IJ: in function stringContainingArray " + i + " is " + stringContainingArray); // for debugging
		}
		return stringContainingArray;
	} 
	
/*	// this function casts ArrayList into Array and calls the getStringFromArray function
	private static String getStringFromArrayList(ArrayList stringArrayList, String delimiter) { 
		String[] stringArray = new String[stringArrayList.size()];
		stringArray = (String[]) stringArrayList.toArray();
		return getStringFromArray(stringArray, delimiter);
	} 
	
	// this function casts List into Array and calls the getStringFromArray function
	private static String getStringFromList(List stringList, String delimiter) { 
		String[] stringArray = new String[stringList.size()];
		stringArray = (String[]) stringList.toArray(stringArray);
		return getStringFromArray(stringArray, delimiter);
	} 
*/	
	// this function analyses a regular expression with named groups (e.g. (?<myName>[a-Z]*)) and gives back an array of the names of the groups
	private static String[] getNamedGroupCandidates(String regexString) {
		String[] namedGroups = new String[100];                                // array big enough to handle up to 100 regex 
		namedGroups[0] = "queryString";                                        // the first element of a regex match is always the entire queryString, since this is not present in the group names in regex, it is assigned here as first element
		Pattern r = Pattern.compile("\\(\\?<([a-zA-Z][a-zA-Z0-9]*)>");         // regex searching for pattern to find group names "?<>"
		Matcher m = r.matcher(regexString);
		int i = 1;                                                             // element 0 is already fill, see above
		while (m.find()) {
			namedGroups[i] = (String) m.group(1);
			//IJ.log("IJ: pattern names are:" + m.group(1));  //for debugging
			++i;
		}
		// generate a new array of actually the size of the found group names, put all group names there (null is excluded) and use a as return string
		String[] resizedNamedGroups = new String[i];
		System.arraycopy(namedGroups, 0, resizedNamedGroups, 0, i);
		IJ.log("MacroExtensions: regex group names are: " + getStringFromArray(resizedNamedGroups, ", "));  // write to IJ-Log window 
		return resizedNamedGroups;
	}
	
	// this function transforms all content of a LinkedHashMap into a single string by concatenating first all the keys and then all the values (HashSets themselves will be concatenated into String) 
	private static String getStringFromHashMap(LinkedHashMap<String, HashSet> hashMapOfHashSets, String delimiterHashSets, String delimiterValues) {
		// Create a string variable that contains all lists as one big String separated by delimiters
		String resultString = new String();
		
		// KEYS: keyNameArray stores the named of the groups from regex pattern and is returned as first list then concatenated into a single string
		String[] keyNameArray = new String[hashMapOfHashSets.size()];
		keyNameArray = (String[]) hashMapOfHashSets.keySet().toArray(keyNameArray);                 // the keySet of a HashMap seems to be a List/ArrayList, the .toArray() method returns an object an must be cast to String[]
		resultString = getStringFromArray(keyNameArray, delimiterValues);                           // make string from Array
		//IJ.log("IJ:" + resultString);
				
		// VALUES: hashValueArray stores the regex matches found and stored as HashSet; those are cast into String[] array and then concatenated into a single string and then concatenated to resultString
		String[] hashValueArray = new String[0];	
		for (Map.Entry<String, HashSet> mapEntry : hashMapOfHashSets.entrySet()) {                              // the Map.Entry is helpful for iteration over the HashMap, then value can be accessed by .getValues and keys by .getKeys 
			hashValueArray = ((String[]) mapEntry.getValue().toArray(new String[mapEntry.getValue().size()]));  // .getValue is from type HashSet, .toArray returns an object that must be cast into a new String[] array of the length / size of the current HashSet  
			//IJ.log("IJ:" + ((String) mapEntry.getValue().toArray(new String[0])[0]));                         // for debugging
			Arrays.sort(hashValueArray);                                                                        // sorts the values found
			resultString += delimiterHashSets + getStringFromArray(hashValueArray, delimiterValues);                                // make string from Array
		}
		return resultString;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
	    if (!IJ.macroRunning()) {
	        IJ.error("Cannot install extensions from outside a macro!");
	        return;
	      }
	 //   getExtensionFunctions(); 
	    
	   Functions.registerExtensions(this);
	   //IJ.log("IJ: registerExtensions done");
	}
	
    /**
     * This main function serves for development purposes.
     * It allows you to run the plugin immediately out of
     * your integrated development environment (IDE).
     *
     * @param args whatever, it's ignored
     * @throws Exception
     */
    public static void main(final String... args) throws Exception {
        /* create the ImageJ application context with all available services
        final ImageJ ij = new ImageJ();
        ij.ui().showUI();

        // ask the user for a file to open
        final File file = ij.ui().chooseFile(null, "open");

        if (file != null) {
            // load the dataset
            final Dataset dataset = ij.scifio().datasetIO().open(file.getPath());

            // show the image
            ij.ui().show(dataset);

            // invoke the plugin
            ij.command().run(FijiToolsForHCSplugin.class, true);
        }
        */
    }

}
