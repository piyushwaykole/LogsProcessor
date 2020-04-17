package edu.asu.ets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.Scanner;

public class LogCombiner {

	public static final String DELIMITER = "/";

	public static void combine(String args) {

		LogCombiner logc = new LogCombiner();
		String[] allfiles = logc.getAllFiles(args);
		String destFileName = args + DELIMITER + "combined.log";
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(destFileName, true));
			logc.combine(writer, args, allfiles);
			writer.close();

		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private String[] getAllFiles(String dirPath) {
		File files = new File(dirPath);
		String[] allfiles = null;
		if (files.exists() && files.isDirectory()) {
			allfiles = files.list();
		}

		Arrays.sort(allfiles, (a, b) -> {

			String[] splita = a.split("\\.");
			String[] splitb = b.split("\\.");

			if (splita[2].compareTo(splitb[2]) == 0) {

				int montha = Integer.parseInt(splita[0]);
				int monthb = Integer.parseInt(splitb[0]);
				if (montha == monthb) {
					int daya = Integer.parseInt(splita[1]);
					int dayb = Integer.parseInt(splitb[1]);
					return daya - dayb;
				}
				return montha - monthb;
			}
			return splita[2].compareTo(splitb[2]);
		});
		System.out.println(Arrays.toString(allfiles));
		return allfiles;
	}

	private void combine(BufferedWriter handle, String dirpath, String[] allfiles) {

		for (String file : allfiles) {
			String path = dirpath + DELIMITER + file;

			File filehandle = new File(path);
			if (filehandle.exists()) {
				try {
					Scanner scanner = new Scanner(filehandle);
					handle.append("-------------------------------" + file + "-------------------------------" + "\n");
					while (scanner.hasNextLine()) {
						handle.append(scanner.nextLine());
						handle.append("\n");
					}
					scanner.close();
					handle.newLine();

				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}
}
