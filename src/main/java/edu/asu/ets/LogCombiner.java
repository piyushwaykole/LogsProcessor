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

public class LogCombiner {

	public static final String LICENSE_FILE_PREFIX = "license.log";
	public static final String TIMESTAMP = "TIMESTAMP";

	public static void main(String[] args) {

		LogCombiner logc = new LogCombiner();
		String[] filenames;
		try {
			filenames = logc.getLicenseFileNames(args[0]);

			for (String f : filenames) {
				System.out.println(f);
			}
		} catch (FileNotFoundException e) {
			System.out.println(e.getMessage() + " --> Check Folder path and try again");
			return;
		}

		for (String f : filenames) {

			String file = args[0] + "\\" + "\\" + f;
			System.out.println(file);
			try {
				Scanner scanner = new Scanner(new File(file));
				Map<String, StringBuilder> m = logc.createSeperateFiles(scanner);
				logc.saveMapToFiles(args[0], m);
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

		}

	}

	private String[] getLicenseFileNames(String dirPath) throws FileNotFoundException {

		File folderName = new File(dirPath);

		if (folderName.exists() && folderName.isDirectory()) {

			// Filter to only get license files from that folder
			FilenameFilter licenseFilter = new FilenameFilter() {

				public boolean accept(File dir, String name) {
					if (name.startsWith(LICENSE_FILE_PREFIX))
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

		String currentDate = null;

		String line = "";
		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			if (line.contains(TIMESTAMP))
				break;

			unknown.append(line);
			unknown.append("\n");

		}

		firstDateInFile = line.split(" ")[3];
		firstDateCount++;

		currentDate = firstDateInFile;
		StringBuilder curr = new StringBuilder();

		while (scanner.hasNextLine()) {
			line = scanner.nextLine();

			if (line.contains(TIMESTAMP)) {
				String[] splitString = line.split(" ");

				if (!currentDate.equals(splitString[3])) {
					map.put(currentDate, curr);
					curr = new StringBuilder();
					currentDate = splitString[3];
				}

				if (currentDate.equals(firstDateInFile))
					firstDateCount++;
			}

			curr.append(line);
			curr.append("\n");

		}

		map.put(currentDate, curr);

		if (firstDateCount < 8) {
			unknown.append(map.get(firstDateInFile).toString());
			map.put(firstDateInFile, unknown);
		}

		String prevDate = getPreviousDate(firstDateInFile);
		if (prevDate != null)
			map.put(prevDate, unknown);

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
			String fileName = dirPath + "\\" + "\\" + getFileName(entry.getKey());

			File file = new File(fileName);
			BufferedWriter writer = null;
			if (file.exists()) {
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
