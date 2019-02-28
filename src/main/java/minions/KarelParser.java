package minions;


import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.*;

import javaEval.CharSequenceJavaFileObject;
import javaEval.ClassFileManager;
import javaEval.JavaSource;

import javax.swing.event.ListSelectionEvent;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileManager;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

import util.FileSystem;
import util.Warnings;


public class KarelParser {

	TokenScanner scanner;

	boolean correctlyCarriageReturned = false;
	int indentationErrors = 0;
	boolean parsed = true;
	int indentLevel = 0;

	public int getIndentationErrors() {
		return indentationErrors;
	}

	public void parse(String karelCode) {
		indentationErrors = 0;
		scanner = new TokenScanner(karelCode);
		indentLevel = 0;

		while(true) {
			List<String> line = getNextLine();
			if(line.isEmpty()) {
				return;
			}
			if(!isWhitespace(line)) {
				lookForCodeBlockEnd(line);
				if(hasIndentationError(line)) {
					indentationErrors++;
				}
				lookForCodeBlockStart(line);
			}
		}
		
		

	}

	private boolean isWhitespace(List<String> line) {
		for(String str : line) {
			if(!str.trim().isEmpty()) {
				return false;
			}
		}
		return true;
	}

	private void lookForCodeBlockEnd(List<String> line) {
		if(line.contains("}")) {
			indentLevel--;
			indentLevel = Math.max(indentLevel, 0);
		}
	}

	private void lookForCodeBlockStart(List<String> line) {
		if(line.contains("{")) {
			indentLevel++;
		}
	}

	private boolean hasIndentationError(List<String> line) {
		if(line.size() < indentLevel) {
			return true;
		} else {
			// make sure it starts with enough tabs
			for(int i = 0; i < indentLevel; i++) {
				if(!line.get(i).equals("\t")) {
					return true;
				}
			}
			// but doesn't have too many
			if(line.get(indentLevel).equals("\t")) {
				return true;
			} 
		}
		return false;
	}

	private List<String> getNextLine() {
		List<String> lineTokens = new ArrayList<String>();
		while(scanner.hasMoreTokens()) {
			String nextToken = scanner.nextToken();
			if(nextToken.equals("\n")) {
				if(!lineTokens.isEmpty()) {
					return lineTokens;
				}
			} else {
				lineTokens.add(nextToken);
			}
		}

		return lineTokens;
	}
	
	public static void main(String[] args) {
		String code = "/*\n" + 
				" * File: StoneMasonKarel.java\n" + 
				" * --------------------------\n" + 
				" * The StoneMasonKarel subclass as it appears here does nothing.\n" + 
				" * When you finish writing it, it should solve the \"repair the quad\"\n" + 
				" * problem from Assignment 1.  In addition to editing the program,\n" + 
				" * you should be sure to edit this comment so that it no longer\n" + 
				" * indicates that the program does nothing.\n" + 
				" */\n" + 
				"\n" + 
				"import stanford.karel.*;\n" + 
				"\n" + 
				"public class StoneMasonKarel extends SuperKarel {\n" + 
				"\n" + 
				"	// Precondition: None\n" + 
				"	// Postcondition: All columns have been fixed\n" + 
				"	public void run() {\n" + 
				"		while (frontIsClear()) {\n" + 
				"			addStonesToCurrentColumn();\n" + 
				"			moveDownColumn();\n" + 
				"			moveToNextColumn();\n" + 
				"		}\n" + 
				"		//These additional two methods prevent the fencepost error that results from the whileloop not accounting for the last column after hitting wall.\n" + 
				"		addStonesToCurrentColumn();\n" + 
				"		moveDownColumn();\n" + 
				"	}\n" + 
				"	\n" + 
				"\n" + 
				"	// Makes Karel move up column she is on while detecting whether beeper is present. \n" + 
				"	// When beeper is not present, Karel places beeper, thus fixing column.\n" + 
				"	// Final if-statement prevents fencepost error that results from whileloop not accounting for last position after hitting wall.\n" + 
				"	// Precondition: None\n" + 
				"	// Postconditions: \n" + 
				"	private void addStonesToCurrentColumn() {\n" + 
				"		// Karel must turn left to face north and move up.\n" + 
				"		turnLeft();\n" + 
				"		while (frontIsClear()) {\n" + 
				"			if (beepersPresent()) {\n" + 
				"				move();\n" + 
				"			} else {\n" + 
				"				putBeeper();\n" + 
				"				move();\n" + 
				"			}\n" + 
				"		}\n" + 
				"		if (noBeepersPresent()) {\n" + 
				"			putBeeper();\n" + 
				"		}\n" + 
				"	}\n" + 
				"\n" + 
				"	// Makes Karel turn after hitting wall at top of column and move down column until wall at bottom of column is hit.\n" + 
				"	// Turn left makes Karel face same direction as initial direction (east).\n" + 
				"	private void moveDownColumn() {\n" + 
				"		turnAround();\n" + 
				"		while (frontIsClear()) {\n" + 
				"			move();\n" + 
				"		}\n" + 
				"		turnLeft();\n" + 
				"	}\n" + 
				"\n" + 
				"	// Makes Karel move to next column, which is four spaces away.\n" + 
				"	private void moveToNextColumn() {\n" + 
				"		// Karel will keep moving right until the world ends\n" + 
				"		move();\n" + 
				"		move();\n" + 
				"		move();\n" + 
				"		move();\n" + 
				"	}\n" + 
				"}";
		
		KarelParser parser = new KarelParser();
		parser.parse(code);
		System.out.println(parser.indentationErrors);
	}


}
