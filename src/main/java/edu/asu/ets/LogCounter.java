package edu.asu.ets;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

public class LogCounter {

	public static final String OUT = "OUT:";
	private Map<String, Integer> countMap =  new HashMap<String, Integer>();
	public static final String DELIMITER = "/";
	
	public void cleanUpMaps(Map<String, StringBuilder> map, String userlist, String filepath) throws FileNotFoundException {
		File file = new File(userlist);
		Set<String> users = new HashSet<String>();
		if(file.exists()) {
			Scanner scanner = new Scanner(file);
			
			while(scanner.hasNextLine())
			{
				String nextline = scanner.nextLine();
				users.add(nextline.trim());
			}
			scanner.close();
		}
		List<String> toberemoved = new ArrayList<String>();
		for(String user: users) {
			
			if(map.containsKey(user))
			{
				map.remove(user);
				toberemoved.add(user);
			}
		}
		for(String st: toberemoved)
			users.remove(st);
		
		String unusedUsers = filepath + DELIMITER + "unused.users";
		
		try {
			BufferedWriter writer = new BufferedWriter(new FileWriter(unusedUsers));
			
			writer.write("Users from the userlist which did not used services - " + "\n");
			writer.write(users.toString() + "\n");
			writer.write("--------------------------------------------------------------" + "\n");
			writer.write("Users who were not found in the userlist but used services - " + "\n");
			writer.write(map.keySet().toString());
			
			writer.close();
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
	public void countLogs(String filepath, String dirpath, String userlist) throws IOException {
		
		File combinedfile = new File(filepath);
		
		if(combinedfile.exists()) {
			
			Scanner scanner = new Scanner(combinedfile);
			Map<String, StringBuilder> map = parseLogs(scanner);
			scanner.close();
			
			File logFolder = new File(dirpath + DELIMITER + "counts");
			if (!logFolder.exists()) {
				logFolder.mkdir();
			}

			saveMapToFiles(map,logFolder.getPath());
			
			LogCombiner.combine(countMap, logFolder.getPath());

			cleanUpMaps(map,userlist ,logFolder.getPath());
			
		}else {
			throw new FileNotFoundException("-Combined File path is incorrect, please check and run again");
		}
	}
	
	private Map<String, StringBuilder> parseLogs(Scanner scanner){
		
		Map<String, StringBuilder> map = new HashMap<String, StringBuilder>();
		
		while(scanner.hasNextLine()) {
			
			String nextline = scanner.nextLine();
			
			if(nextline.contains(OUT)) {
				
				String[] split = nextline.split(" ");
				
				for(int i=4;i<split.length;i++)
				{
					int index = split[i].indexOf('@');
					if(index != -1) {
						
						String username = split[i].substring(0, index);
						
						if(!map.containsKey(username))
							map.put(username, new StringBuilder());
						
						if(!countMap.containsKey(username))
							countMap.put(username, 0);
						
						StringBuilder sb = map.get(username);
						sb.append(nextline);
						sb.append("\n");
						Integer count = countMap.get(username);
						countMap.put(username, count + 1);
						
						break;
					}
				}
			}

		}
		
		return map;

	}
	
	private void saveMapToFiles(Map<String,StringBuilder> map, String filePath) throws IOException {
		
		for(Map.Entry<String, StringBuilder> entry: map.entrySet()) {
			
			String filepath = filePath + DELIMITER + entry.getKey();

			BufferedWriter writer = null;
			
				try {
					writer = new BufferedWriter(new FileWriter(filepath));
					writer.write("The Count for user " + entry.getKey() +" : "+ countMap.get(entry.getKey()) + "\n");
					writer.write("------------------------------------------------------------" + "\n");
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
