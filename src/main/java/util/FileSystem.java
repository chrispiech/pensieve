package util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import org.json.JSONObject;

public class FileSystem {

	private static final String dataDirPath = "../dataRaw";

	private static String assnId = null;

	public static void setAssnId(String assnId) {
		System.out.println("Assignment Id: " + assnId);
		FileSystem.assnId = assnId;
	}

	public static String getAssnId() {
		return assnId;
	}
	
	public static File getAssnDir() {
		File dataDir = new File(dataDirPath);
		return new File(dataDir, getAssnId());
	}
	
	public static File getDataDir() {
		return new File(dataDirPath);
	}

	/** 
	 * Returns the file contents (as a string) of the
	 * given file
	 */
	public static String getFileContents(File file) {
		Scanner codeIn = null;
		String content = "";
		try {
			codeIn = new Scanner(file);
			while (codeIn.hasNextLine()) {
				content += codeIn.nextLine() + "\n";
			}
		} catch(Exception e) {
			return null;
		} finally {
			if (codeIn != null) {
				codeIn.close();
			}
		}
		return content;
	}

	public static List<String> getFileLines(File file) {
		Scanner codeIn = null;
		try {
			ArrayList<String> content = new ArrayList<String>();
			codeIn = new Scanner(file);
			while (codeIn.hasNextLine()) {
				content.add(codeIn.nextLine());
			}
			codeIn.close();
			return content;
		} catch(Exception e) {
			if(codeIn != null)codeIn.close();
			return null;
		} 
	}
	
	public static Map<String, Integer> getFileMap(File file) {
		return getFileMap(file, "\t");
	}
	
	@SuppressWarnings("unchecked")
	public static<T> Set<T> getFileSet(File file) {
		List<String> lines = FileSystem.getFileLines(file);
		if(lines == null) return null;
		Set<T> fileSet = new HashSet<T>();
		for(String line : lines) {
			fileSet.add((T)(line));
		}
		return fileSet;
	}

	public static Map<String, Integer> getFileMap(File file, String delimiter) {
		Map<String, Integer> map = new HashMap<String, Integer>();
		List<String> lines = FileSystem.getFileLines(file);
		for(String line : lines) {
			String[] row = line.split(delimiter);
			if(row.length != 2) {
				throw new RuntimeException("There should only be two cols");
			}
			String id = row[0];
			int count = Integer.parseInt(row[1]);
			map.put(id, count);
		}
		return map;
	}
	
	public static int[][] getFileMatrix(File file) {
		List<String> lines = FileSystem.getFileLines(file);
		int rows = lines.size();
		String firstRow = lines.get(0);
		int cols = firstRow.split(",").length;
		
		int[][] matrix = new int[rows][cols];
		
		for(int r = 0; r < rows; r++) {
			String line = lines.get(r);
			String[] csvCols = line.split(",");
			for(int c = 0; c < cols; c++) {
				String strValue = csvCols[c];
				int value = Integer.parseInt(strValue);
				matrix[r][c] = value;
			}
		}
		return matrix;
	}

	public static Map<String, String> getFileMapString(File file) {
		Map<String, String> map = new HashMap<String, String>();
		List<String> lines = FileSystem.getFileLines(file);
		for(String line : lines) {
			String[] row = line.split(",");
			if(row.length != 2) {
				System.out.println(line);
				throw new RuntimeException("There should only be two cols");
			}
			String id = row[0].trim();
			String value = row[1].trim();
			if(!value.equals("null")) {
				map.put(id, value);
			}
		}
		return map;
	}
	
	public static void createFile(File f, String text) {
		FileWriter file = null;
		try {

			file = new FileWriter(f);
			file.write(text);
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			if (file != null) {
				try {
					file.close();
				} catch (IOException ex) {
					// ignore
				}
			}
		}
	}

	public static void createFile(File dir, String fileName, String text) {
		dir.mkdirs();
		File f = new File(dir, fileName);
		createFile(f, text);
	}
	
	public static void createMapFile(File dir, String fileName, Map<String, String> map) {
		String txt = "";
		for(String key : map.keySet()) {
			String value = map.get(key);
			txt += key + ","+ value + "\n";
		}
		createFile(dir, fileName, txt);
	}
	
	public static void createFile(File dir, String fileName, List<String> lines) {
		String txt = "";
		for(int i = 0; i < lines.size(); i++) {
			txt += lines.get(i);
			if(i != lines.size() - 1) {
				txt += "\n";
			}
		}
		createFile(dir, fileName, txt);
	}


	public static List<File> listNumericalFiles(File path) {
		if(!path.exists()) return new ArrayList<File>();

		// Get just the number files
		List<File> fileList = new ArrayList<File>();
		for(File f : path.listFiles()) {
			String name = getNameWithoutExtension(f);
			try {
				int intName = Integer.parseInt(name);
				fileList.add(f);
			} catch(NumberFormatException e)  {}
		}

		// Sort the number files
		Collections.sort(fileList, new Comparator<File>() {
			public int compare(File f1, File f2) {
				String n1 = getNameWithoutExtension(f1);
				String n2 = getNameWithoutExtension(f2);
				try {
					int i1 = Integer.parseInt(n1);
					int i2 = Integer.parseInt(n2);
					return i1 - i2;
				} catch(NumberFormatException e) {
					throw new AssertionError(e);
				}
			}
		});
		return fileList;
	}

	public static List<File> listFiles(File path) {
		if(!path.exists()) return new ArrayList<File>();
		return new ArrayList<File>(Arrays.asList(path.listFiles()));
	}
	
	public static List<File> listDirs(File path) {
		List<File> dirs = new ArrayList<File>();
		for(File f : listFiles(path)) {
			if(f.isDirectory()) {
				dirs.add(f);
			}
		}
		return dirs;
	}

	public static String getNameWithoutExtension(File file) {
		String fileName = file.getName();
		if(!fileName.contains(".")) {
			return fileName;
		}
		String subName = "";
		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
			subName = fileName.substring(0, i);
		}
		return subName;
	}
	
	public static String getExtension(File file) {
		String fileName = file.getName();
		String extension = "";
		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
			extension = fileName.substring(i+1);
		}
		return extension;
	}


	public static void copyFile(String from, String to) {
		File source = new File(from);
		File dest = new File(to);

		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			dest.createNewFile();
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			inputChannel.close();
			outputChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			if (inputChannel != null) {
				try {
					inputChannel.close();
				} catch (IOException ex) {}
			}
			if (outputChannel != null) {
				try {
					outputChannel.close();
				} catch (IOException ex) {}
			}
		}
	}

	public static JSONObject loadJson(File f) {
		String jsonStr = FileSystem.getFileContents(f);
		JSONObject json = new JSONObject(jsonStr);
		return json;
	}

	

	/*
	 * Here is some old code for working with files... just for reference.
	 * 
	 * 

	private static String root = "../../";


	public static String getEquivalencePath() {
		return FileSystem.getDataPath() + "/equivalences/";
	}

	public static String getMethodGroupsPath() {
		return FileSystem.getDataPath() + "/methodGroups/";
	}

	public static String getAppliedDomainsPath() {
		return FileSystem.getDataPath() + "/appliedDomains/";
	}

	public static String getGraphsPath() {
		return FileSystem.getDataPath() + "/graphs/";
	}

	public static String getUnitTesterSrcDir() {
		return root + "src/UnitTester/";
	}

	public static File getUnitTestFile() {
		return new File(FileSystem.getDataPath() + "unitTests/tests.txt");
	}

	public static String getDataPath() {
		return root + "data/" + dataSet + "/" + assnStr + "/";
	}

	public static String getSubmissionsPath() {
		return getDataPath() + "submissions/";
	}

	public static String getCorrectsPath() {
		return getDataPath() + "/unitTestResults/corrects.txt";
	}

	public static String getUnitTestResultDir(String id) {
		return getDataPath() + "/unitTestResults/" + id + "/";
	}

	public static String getUnitTestDir() {
		return getDataPath() + "/unitTests/";
	}

	public static int getNumOpenFiles() {
		String [] args = {"lsof", "|", "wc", "-l"};
		String result = execCmd(args);
		System.out.println(result);
		return Integer.parseInt(result);
	}

	public static String execCmd(String[] args) {
		Runtime r = Runtime.getRuntime();
		BufferedReader in = null;
		Process p = null;
		String output = "";
		try {
			p = r.exec(args);
			in = new BufferedReader(  
					new InputStreamReader(p.getInputStream()));  
			String line = null;  
			p.waitFor();
			while ((line = in.readLine()) != null) {  
				output += line + "\n"; 
			}  
		} catch (IOException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
	        if (in != null) {
	            try {
	            	in.close();
	            } catch (IOException ex) {}
	        }
	        if(p != null) {
	        	try{
		        	p.getInputStream().close();
		        	p.getOutputStream().close();
		        	p.getErrorStream().close();
	        	} catch(IOException ex) {
	        		throw new RuntimeException("fail");
	        	}
	        }
		}

		return output;
	}

	public static void copyFile(String from, String to) {
		File source = new File(from);
		File dest = new File(to);

		FileChannel inputChannel = null;
		FileChannel outputChannel = null;
		try {
			dest.createNewFile();
			inputChannel = new FileInputStream(source).getChannel();
			outputChannel = new FileOutputStream(dest).getChannel();
			outputChannel.transferFrom(inputChannel, 0, inputChannel.size());
			inputChannel.close();
			outputChannel.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
	        if (inputChannel != null) {
	            try {
	            	inputChannel.close();
	            } catch (IOException ex) {}
	        }
	        if (outputChannel != null) {
	            try {
	            	outputChannel.close();
	            } catch (IOException ex) {}
	        }
		}
	}

	public static ArrayList<String> loadCode(int astId) {
		throw new RuntimeException("depricated");

	}

	

	public static List<File> listDirs(File studentDir) {
		return listDirs(studentDir.getAbsolutePath());
	}

	public static List<File> listFiles(String path) {
		File f = new File(path);
		if(!f.exists()) return new ArrayList<File>();
		return new ArrayList<File>(Arrays.asList(f.listFiles()));
	}

	public static List<File> listFiles(File dir) {
		return new ArrayList<File>(Arrays.asList(dir.listFiles()));
	}

	public static void createEmptyFile(String dir, String fileName) {
		new File(dir).mkdirs();
		FileWriter file = null;
		try {
			String path = dir + "/" + fileName;
			file = new FileWriter(path);
			file.write("");
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        if (file != null) {
	            try {
	            	file.close();
	            } catch (IOException ex) {
	                // ignore
	            }
	        }
		}
	}

	public static List<String> getFileLines(String path) {
		Scanner codeIn = null;
		try {
			ArrayList<String> content = new ArrayList<String>();
			codeIn = new Scanner(new File(path));
			while (codeIn.hasNextLine()) {
				content.add(codeIn.nextLine());
			}
			codeIn.close();
			return content;
		} catch(Exception e) {
			if(codeIn != null)codeIn.close();
			return null;
		} 
	}

	public static String getFileContents(String path) {
		Scanner codeIn = null;
		try {
			String content = "";
			codeIn = new Scanner(new File(path));
			while (codeIn.hasNextLine()) {
				content += codeIn.nextLine() + "\n";
			}
			codeIn.close();
			return content;
		} catch(Exception e) {
		} finally {
	        if (codeIn != null) {
	            codeIn.close();
	        }
		}
		return null;
	}

	public static void createFile(String dir, String fileName,
			String text) {
		new File(dir).mkdirs();
		String path = dir + "/" + fileName;
		FileWriter file = null;
		try {

			file = new FileWriter(path);
			file.write(text);
			file.flush();
			file.close();

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
	        if (file != null) {
	            try {
	            	file.close();
	            } catch (IOException ex) {
	                // ignore
	            }
	        }
		}
	}

	public static int getNumStudents() {
		String programPath = getSubmissionsPath();
		List<File> files = FileSystem.listDirs(programPath);
		return files.size();
	}

	public static void removeFile(String tmp, String name) {
		File file = new File(tmp + "/" + name);
		file.delete();
	}

	public static String getNameWithoutExtension(String fileName) {
		String subName = "";
		int i = fileName.lastIndexOf('.');
		int p = Math.max(fileName.lastIndexOf('/'), fileName.lastIndexOf('\\'));

		if (i > p) {
			subName = fileName.substring(0, i);
		}
		return subName;
	}




	public static void createDir(String path) {
		new File(path).mkdirs();
	}


	public static boolean fileExists(String path) {
		return new File(path).exists();
	}

	public static String getFileContents(File file) {
		return getFileContents(file.getAbsolutePath());
	}

	public static int getNumFiles(String path) {
		return FileSystem.listFiles(path).size();
	}

	public static String getSolution() {
		String dataPath = FileSystem.getDataPath();
		String solnPath = dataPath + "Solution.java";
		return FileSystem.getFileContents(solnPath);
	}

	public static int getLastCommitIndex(String studentDir) {
		int lastCommitIndex = 0;
		while(true) {
			int indexToTest = lastCommitIndex + 1;
			String testPath = studentDir + "/" + indexToTest;
			if(new File(testPath).isDirectory()) {
				lastCommitIndex = indexToTest;
			} else {
				break;
			}
		}
		return lastCommitIndex;
	}

	public static void clearDir(String path) {
		File folder = new File(path);
		clearDir(folder);
	}

	public static void clearDir(File folder) {
		File[] files = folder.listFiles();
	    if(files!=null) { //some JVMs return null for empty dirs
	        for(File f: files) {
	            if(f.isDirectory()) {
	            	FileSystem.clearDir(f);
	            } else {
	                f.delete();
	            }
	        }
	    }
	    folder.delete();
	}*/





}
