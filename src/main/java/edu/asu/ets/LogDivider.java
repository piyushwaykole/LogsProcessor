package edu.asu.ets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Pattern;

public class LogDivider {

	public static final String LICENSE_FILE_ANSYS = "license.log";
	public static final String LICENSE_FILE_ABAQUS = "abaquslm.log";
	public static final String TIMESTAMP = "TIMESTAMP";
	public static final String DELIMITER = "/";
	public static final String UNKNOWN_FILE_NAME = "unknown_logs";

	
	private String firstInEveryFile = "";

	public static void main(String[] args) {

		String rootpath = args[0];
		if (args[0].charAt(args[0].length() - 1) == '/')
			rootpath = args[0].substring(0, args[0].length() - 1);

		LogDivider logc = new LogDivider();
		String[] filenames;
		try {
			filenames = logc.getLicenseFileNames(rootpath);
			System.out.println("License files found :- ");

			for (String f : filenames) {
				System.out.println(f);
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage() + " --> Check Folder path and try again");
			return;
		}

		// create a separate logs folder to save all individual log file
		File logFolder = new File(rootpath + DELIMITER + "logs");
		if (!logFolder.exists()) {
			logFolder.mkdir();
		}

		System.out.println("----------------------------------------------------------------------------------------------");
		System.out.println("Processing log files :-");
		for (String f : filenames) {

			String file = rootpath + DELIMITER + f;
			System.out.println(file);
			try {
				Scanner scanner = new Scanner(new File(file));
				logc.firstInEveryFile = null;
				Map<String, StringBuilder> m = logc.createSeperateFiles(scanner);
				logc.saveMapToFiles(rootpath, m);
				scanner.close();
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}
		String path = rootpath + DELIMITER + "logs";
		String combined = LogCombiner.combine(path);
		
		
		LogCounter logcounter =  new LogCounter();
		
		try {
			logcounter.countLogs(combined, rootpath, args[1]);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private String[] getLicenseFileNames(String dirPath) throws FileNotFoundException {

		File folderName = new File(dirPath);

		if (folderName.exists() && folderName.isDirectory()) {

			// Filter to only get license files from that folder
			FilenameFilter licenseFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.startsWith(LICENSE_FILE_ANSYS) || name.startsWith(LICENSE_FILE_ABAQUS))
						return true;
					else
						return false;
				}
			};

			String[] files = folderName.list(licenseFilter);
			return files;
		}
		throw new FileNotFoundException("Either Folder does not exists or is not a directory");
	}

	private Map<String, StringBuilder> createSeperateFiles(Scanner scanner) {

		Map<String, StringBuilder> map = new HashMap<String, StringBuilder>();
		StringBuilder unknown = new StringBuilder();

		String firstDateInFile = null;
		int firstDateCount = 0;

		boolean doesNotContainTimeStamp = true;
		String currentDate = null;

		String line = "";
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			if (line.contains(TIMESTAMP))
			{
				doesNotContainTimeStamp = false;
				break;	
			}
				
			unknown.append("\n");
			unknown.append(line);
		}

		// check if the file doesnt contain the timestamp.
		if(doesNotContainTimeStamp) {
			map.put(UNKNOWN_FILE_NAME, unknown);
			return map;
		}
		
		String[] splt = line.split(" ");
		firstDateInFile = splt[splt.length - 1];
		firstDateCount++;

		currentDate = firstDateInFile;
		StringBuilder curr = new StringBuilder();

		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			if (line.contains(TIMESTAMP)) {
				String[] splitString = line.split(" ");

				String newDate = splitString[splitString.length - 1];

				if (!currentDate.equals(newDate)) {
					map.put(currentDate, curr);
					curr = new StringBuilder();
					currentDate = newDate;
				}

				if (currentDate.equals(firstDateInFile))
					firstDateCount++;
			}

			curr.append(currentDate + " ");
			curr.append(line);
			curr.append("\n");

		}

		map.put(currentDate, curr);

		if (firstDateCount < 8) {
			String val = Pattern.compile("\n").matcher(unknown).replaceAll("\n" + firstDateInFile + " ");
			unknown = new StringBuilder(val);
			unknown.append(map.get(firstDateInFile).toString());
			map.put(firstDateInFile, unknown);
		} else {
			String prevDate = getPreviousDate(firstDateInFile);
			String val = Pattern.compile("\n").matcher(unknown).replaceAll("\n" + prevDate + " ");
			unknown = new StringBuilder(val);

			if (prevDate != null)
				map.put(prevDate, unknown);

			this.firstInEveryFile = prevDate;

		}

		return map;
	}

	private String getPreviousDate(String date) {
		SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
		Date day;
		try {
			day = formatter.parse(date);
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
		Calendar calender = Calendar.getInstance();
		calender.setTime(day);

		calender.add(Calendar.DAY_OF_YEAR, -1);

		Date prevDate = calender.getTime();
		String result = formatter.format(prevDate);

		return result;

	}

	private String getFileName(String date) {

		StringBuilder st = new StringBuilder();

		for (int i = 0; i < date.length(); i++) {
			if (date.charAt(i) == '/')
				st.append('.');
			else
				st.append(date.charAt(i));
		}

		return st.toString();
	}

	private void saveMapToFiles(String dirPath, Map<String, StringBuilder> map) throws IOException {

		for (Map.Entry<String, StringBuilder> entry : map.entrySet()) {
			String fileName = dirPath + DELIMITER + "logs" + DELIMITER + getFileName(entry.getKey());

			System.out.println("Date found - " + entry.getKey());
			File file = new File(fileName);
			BufferedWriter writer = null;
			if ((entry.getKey().equals(this.firstInEveryFile) || entry.getKey().equals(UNKNOWN_FILE_NAME)) && file.exists()) {
				try {
					writer = new BufferedWriter(new FileWriter(fileName, true));
					writer.append(entry.getValue());
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					if (writer != null)
						writer.close();
				}

			} else {
				try {
					writer = new BufferedWriter(new FileWriter(fileName));
					writer.write(entry.getValue().toString());
					writer.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();

				} finally {
					if (writer != null)
						writer.close();
				}
			}

		}
	}
}
