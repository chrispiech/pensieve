# Pensieve

_For an introduction / demo, see: https://youtu.be/gxAzPMqJ7Ic_

This is a Maven project. Maven is simply a dependency manager for Java (https://maven.apache.org/).

To run (main program is `stanford/edu/gitviewer/GitViewer.java`):
* Set `TEST_REPO_PATH` to the desired folder containing version history to view.
  * Our use case expects a .git history in this folder.
* Run `GitViewer.java` as a Java application.

Description of relevant files:
* `runnable/`: contains drag-and-drop JARs for Deluxe and Lite versions of Pensieve
* `stanford/edu/gitviewer/`: handles overall Pensieve setup and layout
  * `GitViewer.java`: main controller, full version of Pensieve used by Teaching Assistants
  * `GitViewerStudent.java`: condensed version of Pensieve given to students
  * `FileHistory.java`: processes code history (for us, the .git file) into useful formats
  * `CodeEditor.java`: controls center panel of student code displayed per snapshot
  * `Intermediate.java, Parser.java, Util.java`: processes and stores useful metadata about each snapshot (e.g. timestamp, working hours)
* `graphs/`: handles display of Workflow Graphs panel
  * `Indentation.java, RunGraphs.java, SourceLengthGraph.java`: custom graphs that we designed and have used with Pensieve. additional / different ones can be added similarly!
  * `ImageViewPane.java`: handles sizing / resizing of right side panels
* `minions/, util/`: useful utility classes

_Chris Piech (piech@cs.stanford.edu), Annie Hu (annie@cs.stanford.edu), Lisa Yan (yanlisa@stanford.edu)_
