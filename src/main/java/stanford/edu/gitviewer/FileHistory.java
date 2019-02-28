package stanford.edu.gitviewer;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.ObjectReader;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;
import org.eclipse.jgit.revwalk.RevWalk;
import org.eclipse.jgit.treewalk.TreeWalk;

import jdk.nashorn.internal.parser.JSONParser;

public class FileHistory {

	// If the student is idle for > BREAK_MINS, pauses timer tracking total amount of work time
	private static final int BREAK_MINS = 10;

	/** Returns null if the git repo was currupt in any way :-)
	 * Else returns the list of files in the repo.
	 * @param repoPath
	 */
	public static ArrayList<String> getFiles(String repoPath) {
		try {
			Git git = Git.init().setDirectory(new File(repoPath)).call();
			Repository repo = git.getRepository();
			ObjectId from = repo.resolve("refs/heads/master");
			RevWalk revWalk = new RevWalk(repo);
			RevCommit headCommit = revWalk.parseCommit(from);

			revWalk.markStart(headCommit);
			HashSet<String> paths = new HashSet<String>();

			// Collect every file that shows up in any commit
			for (RevCommit commit : revWalk) {
				RevTree masterTree = commit.getTree();
				TreeWalk treeWalk = new TreeWalk(repo);
				treeWalk.addTree(masterTree);
				treeWalk.setRecursive(false);
				while (treeWalk.next()) {

					if (treeWalk.isSubtree()) {
						System.out.println("dir: " + treeWalk.getPathString());
						treeWalk.enterSubtree();
					} else {
						String pathStr = treeWalk.getPathString();
						if(isValidFile(pathStr)) {
							paths.add(pathStr);
						}

					}
				}
				treeWalk.close();
			}

			revWalk.close();
			return new ArrayList<String>(paths);
		} catch (Exception e) {
			return null;
		}
	}

	/** Disregard our autograder files */
	private static boolean isValidFile(String name) {
		String caps = name.toUpperCase();
		if(caps.contains("autograder")) return false;
		return true;
	}

	/** Returns all recorded snapshots for a given file. */
	public static ArrayList<Intermediate> getHistory(String repoPath, String filePath) {
		ArrayList<Intermediate> history = getRawIntermediate(repoPath, filePath); 
		addCommitIndex(history);
		addFilePath(history, filePath);
		addIntermediateTiming(history);
		parseIntermediateCode(history);
		return history;
	}

	/** Helper functions */
	private static void addCommitIndex(ArrayList<Intermediate> history) {
		for(int i = 0; i < history.size(); i++) {
			history.get(i).commitIndex = i;
		}
	}

	private static void addFilePath(ArrayList<Intermediate> history, String filePath) {
		for(Intermediate intermediate : history) {
			intermediate.filePath = filePath;
		}
	}

	private static void parseIntermediateCode(ArrayList<Intermediate> history) {
		for(Intermediate intermediate : history) {
			Parser.parse(intermediate);
		}
	}

	/** Track work time / break time information. */
	private static void addIntermediateTiming(ArrayList<Intermediate> history) {
		int startTime = 0;
		int workingTimeSeconds = 0;
		int lastTime = 0;
		if(!history.isEmpty()) {
			startTime = history.get(0).timeStamp;
			lastTime = startTime;
		}
		for (Intermediate intermediate : history) {
			int time = intermediate.timeStamp;
			boolean tookBreak = false;
			int deltaSeconds = time - lastTime;
			// don't add to working time if student took break
			if(deltaSeconds > BREAK_MINS * 60) {
				deltaSeconds = 0;
				tookBreak = true;
			}
			workingTimeSeconds += deltaSeconds;
			double workingMins = workingTimeSeconds / 60.0;
			double workingHours = workingMins / 60.0;
			intermediate.workingHours = workingHours;
			if(tookBreak) {
				double breakSeconds = time - lastTime;
				double breakMins = breakSeconds / 60.0;
				double breakHours = breakMins / 60.0;
				intermediate.breakHours = breakHours;
			} else {
				intermediate.breakHours = null;
			}

			lastTime = time;
		}

	}

	private static ArrayList<Intermediate> getRawIntermediate(String repoPath, String filePath) {
		ArrayList<Intermediate> history = new ArrayList<Intermediate>();
		try {
			Git git = Git.init().setDirectory(new File(repoPath) ).call();
			Repository repo = git.getRepository();

			RevWalk walk = new RevWalk(repo);
			ObjectId from = repo.resolve("refs/heads/master");
			walk.markStart(walk.parseCommit(from));

			String lastText = "";
			int lastRuns = 0;

			List<RevCommit> commits = new ArrayList<RevCommit>();
			for (RevCommit commit : walk) {
				commits.add(commit);
			}
			Collections.reverse(commits);
			for(RevCommit commit: commits) {
				String currText = getFile(repo, commit, filePath);

				boolean editedFile = !currText.isEmpty() && !currText.equals(lastText);

				Integer runs = getRuns(commit.getShortMessage());

				if(editedFile) {

					Intermediate intr = new Intermediate();
					intr.commitMsg = commit.getShortMessage();
					intr.deltaRuns = calcDeltaRuns(lastRuns, runs);
					intr.code = currText;
					intr.timeStamp = commit.getCommitTime();
					history.add(intr);
					lastText = currText;
				}

				if(runs != null) {
					lastRuns = runs;
				}
			}
			walk.close();

		} catch (Exception e) {
			throw(new RuntimeException(e));
		}
		return history;
	}

	private static int calcDeltaRuns(int lastRuns, Integer runs) {
		if(runs != null)  {
			if(runs < lastRuns) {
				return runs;
			} else {
				return (runs - lastRuns);
			}
		}
		return 0;
	}

	/** This is based on the assumption of the format of the 
	 * commit message. I have built it to be fault tolerant...
	 * @param shortMessage: commit message
	 */
	private static Integer getRuns(String shortMessage) {
		int locRun = shortMessage.indexOf("runs");
		if(locRun == -1) return null;
		try {
			int locNum = locRun + 6; // length of 'runs":'
			int endIndex = shortMessage.length() - 1;
			String numStr = shortMessage.substring(locNum, endIndex);
			return Integer.parseInt(numStr);
		} catch(Exception e) {
			return null;
		}
	}

	private static String getFile(Repository repo, RevCommit commit, String path) throws IOException {
		// Makes it simpler to release the allocated resources in one go
		ObjectReader reader = repo.newObjectReader();

		// Get the revision's file tree
		RevTree tree = commit.getTree();
		// .. and narrow it down to the single file's path
		TreeWalk treewalk = TreeWalk.forPath(reader, path, tree);

		if (treewalk != null) {
			// use the blob id to read the file's data
			byte[] data = reader.open(treewalk.getObjectId(0)).getBytes();
			return new String(data, "utf-8").trim();
		} else {
			return "";
		}
	}



}
