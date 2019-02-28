package stanford.edu.gitviewer;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.List;

import graphs.GraphChooser;
import graphs.ImageViewPane;

import org.apache.commons.io.IOUtils;
import org.json.*;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseEvent;
import javafx.stage.Screen;
import javafx.stage.Stage;
import javafx.util.Duration;
import javafx.scene.layout.BorderPane;
import javafx.scene.Group;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.shape.Rectangle;
import javafx.scene.text.Font;
import javafx.scene.text.Text;
import javafx.scene.text.TextAlignment;
import javafx.scene.web.WebView;
import javafx.geometry.Orientation;
import javafx.geometry.Pos;
import javafx.geometry.Rectangle2D;

/** File: GitViewer.java
 * -------------------------------------
 * Main controller for running Pensieve.
 */
public class GitViewer extends Application {

	// manually set for testing
	private static final String TEST_REPO_PATH = "exampleGits/student";
	private static final String CURR_DIR = ".";
	
	// set this to CURR_DIR for student submissions
	private static final String REPO_PATH = CURR_DIR;
	
	// for milestone predictions
	private static final Color[] MILESTONE_COLORS = {
		Color.WHITE,
		Color.ANTIQUEWHITE,
		Color.GOLD,
		Color.CORAL,
		Color.ORANGERED,
		Color.MEDIUMPURPLE,
		Color.DARKBLUE,
		Color.CORNFLOWERBLUE,
		Color.SKYBLUE,
		Color.DARKCYAN,
		Color.TURQUOISE,
		Color.LIGHTGREEN,
		Color.GREENYELLOW,
		Color.GREEN,
		Color.CYAN,
		Color.DIMGREY,
		Color.DARKGRAY
	};
	
	private static final String[] MILESTONE_BLURBS = {
		"No milestone prediction",
		"Hello world",
		"Single row",
		"Diagonal",
		"Two row",
		"Rectangle",
		"Parallelogram",
		"Right triangle",
		"Column structure",
		"Scalene triangle",
		"Pyramid-like",
		"Offset pyramid",
		"Offset Extra Credit",
		"Perfect",
		"Perfect + EC",
		"Off-track",
		"Brick wall"
	};

	/* Controls timestamp -> image lookups */
	private JSONObject lookup = null;
	private String filename = "";
	
	/* Setup main views */
	private final ComboBox<String> comboBox = new ComboBox<String>();
	private final CodeEditor editor = new CodeEditor("hello world");
	private final ListView<HBox> listView = new ListView<HBox>();
	private List<Intermediate> history = null;
	private ImageViewPane progressView = new ImageViewPane();
	private GraphChooser bottomGraph = new GraphChooser("SourceLength");

	public static void main(String[] args) {
		launch(args);
	}
	
	/* Decreases delay between mouse hover and tooltip popup. */
	public static void hackTooltipStartTiming(Tooltip tooltip) {
	    try {
	        Field fieldBehavior = tooltip.getClass().getDeclaredField("BEHAVIOR");
	        fieldBehavior.setAccessible(true);
	        Object objBehavior = fieldBehavior.get(tooltip);

	        Field fieldTimer = objBehavior.getClass().getDeclaredField("activationTimer");
	        fieldTimer.setAccessible(true);
	        Timeline objTimer = (Timeline) fieldTimer.get(objBehavior);

	        objTimer.getKeyFrames().clear();
	        objTimer.getKeyFrames().add(new KeyFrame(new Duration(250)));
	    } catch (Exception e) {
	        e.printStackTrace();
	    }
	}
	
	/* Setup display, initialize image lookup. */
	@Override
	public void start(Stage primaryStage) {
		makeDisplay(primaryStage);
		try {
            InputStream is = new FileInputStream(REPO_PATH + "/images/lookup.json");
            String jsonTxt = IOUtils.toString(is, "UTF-8");
            lookup = new JSONObject(jsonTxt);
    	} catch (Exception e) {
    		System.out.println(e);
    	}
		displayFile(comboBox.getValue());
	}

	/* Displays initial view for selected file. */
	private void displayFile(String filePath) {
		editor.resetScroll();
		progressView.setImageView(null);
		history = FileHistory.getHistory(REPO_PATH, filePath);
		JSONObject fileJSON = null;
		try {
			fileJSON = lookup.getJSONObject(filePath);
		} catch (Exception e) {
			System.out.println("No image directory for file " + filePath);
		}
		makeListView(history, fileJSON);
		bottomGraph.drawGraph(history);
		filename = filePath;
	}
	
	/* Creates colored rectangle with corresponding tooltip for a given commit and milestone. */
	private Rectangle createMilestoneMarker(String timeStamp, JSONObject fileJSON, double height) {
		int milestone = -1;
		int error = 0;
		String tip = "";
		if (fileJSON != null) {
			try {
				JSONObject interJSON = fileJSON.getJSONObject(timeStamp);
				milestone = interJSON.getInt("milestone");
				error = interJSON.getInt("type");
			} catch(Exception e) {
				System.out.println("No matching data for timestamp " + timeStamp);
			}
		}
		
		Rectangle rect = new Rectangle(height, height);
		if (error == 0) {
			if (milestone == -1) {
				rect.setFill(Color.WHITE);
			} else {
				rect.setFill(MILESTONE_COLORS[milestone]);
				tip = "Milestone " + Integer.toString(milestone) + ": " + MILESTONE_BLURBS[milestone];
			}
		} else if (error == 1) {
			rect.setFill(Color.VIOLET);
			tip = "Compile error";
		} else {
			rect.setFill(Color.CRIMSON);
			tip = "Runtime error";
		}
		if (!tip.isEmpty()) {
			Tooltip t = new Tooltip(tip);
			Tooltip.install(rect, t);
			hackTooltipStartTiming(t);
		}
		return rect;
	}

	/* Creates list of all intermediate commits in assignment timeline pane. */
	private ListView<HBox> makeListView(List<Intermediate> history, JSONObject fileJSON) {
		ObservableList<HBox> data = FXCollections.observableArrayList();
		listView.setMinSize(200, 200);
		for (int i = 0; i < history.size(); i++) {
			Intermediate intermediate = history.get(i);
			double workingHours = intermediate.workingHours;
			String text = i +"\t" + formatTime(workingHours);
			if(intermediate.breakHours != null) {
				double breakHours = intermediate.breakHours;
				text += " (" + formatTime(breakHours) + ")";
			}
			String timeStamp = Integer.toString(intermediate.timeStamp);
			HBox pane = new HBox();
			Region filler = new Region(); // makes sure milestone marker is right-aligned
			HBox.setHgrow(filler, Priority.ALWAYS);
			Text label = new Text(text);
			Rectangle marker = createMilestoneMarker(timeStamp, fileJSON, label.getBoundsInLocal().getHeight());
			pane.getChildren().addAll(label, filler, marker);
			data.add(pane);
		}
		listView.setItems(data);
		Intermediate codeVersion = history.get(0); // start at time 0
		String code = codeVersion.code;
		editor.setCode(code);
		return listView;
	}

	private String formatTime(double workingHours) {
		int hours = (int)workingHours;
		int mins = (int) Math.round(60 * (workingHours - hours));
		return hours + "h " + mins + "m";
	}

	/* Changes code displayed and red line in workflow graph given a selected commit. */
	private void onIntermediateSelection(int index) {
		Intermediate codeVersion = history.get(index);
		String code = codeVersion.code;
		editor.setCode(code);
		String timeStamp = Integer.toString(codeVersion.timeStamp);
		try {
			JSONObject interJSON = lookup.getJSONObject(filename).getJSONObject(timeStamp);
			String imgName = interJSON.getString("img_dest");
			makeImageView(imgName);
		} catch(Exception e) {
			System.out.println("image file not found.");
		}
		bottomGraph.setSelectedTime(codeVersion.workingHours);
	}

	/* Displays image preview of current snapshot in the top right view. */
	private void makeImageView(String imgName) {
		Image img = new Image("file:" + REPO_PATH + "/" + imgName);
		ImageView imgView = new ImageView();
		imgView.setImage(img);
		imgView.setSmooth(true);
		imgView.setCache(true);
		imgView.setPreserveRatio(true);
		progressView.setImageView(imgView);
	}
	
	/* Creates the overall layout of Pensieve. */
	private void makeDisplay(final Stage primaryStage) {
		primaryStage.setTitle("CS106A Pensieve");        
		listView.getSelectionModel().selectedItemProperty().addListener(
				new ChangeListener<HBox>() {
					public void changed(ObservableValue<? extends HBox> ov, 
							HBox oldValue, HBox newValue) {
						int index = listView.getSelectionModel().getSelectedIndex();
						if(index == -1) return;
						onIntermediateSelection(index);
					}
				});

		SplitPane graphCodeSplit = new SplitPane();
		graphCodeSplit.getItems().add(listView);
		
		WebView editorView = editor.getView(); 
		graphCodeSplit.getItems().add(editorView);

		SplitPane graphImageSplit = new SplitPane();
		graphImageSplit.setOrientation(Orientation.VERTICAL);
		graphImageSplit.getItems().add(progressView);
		graphImageSplit.getItems().add(bottomGraph.getView());
		graphImageSplit.setDividerPositions(0.5);

		graphCodeSplit.getItems().add(graphImageSplit);
		graphCodeSplit.setDividerPositions(0.12, 0.65);
		BorderPane border = new BorderPane();
		border.setCenter(graphCodeSplit);

		Label fileLabel = new Label("File: ");
		List<String> allFiles = FileHistory.getFiles(REPO_PATH);
		ObservableList<String> options = 
				FXCollections.observableArrayList(allFiles);
		comboBox.setItems(options);
		comboBox.setValue(comboBox.getItems().get(0));

		HBox hb = new HBox();
		hb.setAlignment(Pos.CENTER);
		hb.getChildren().addAll(fileLabel, comboBox);
		hb.setSpacing(10);
		border.setTop(hb);

		comboBox.setOnAction(new EventHandler<ActionEvent>() {

			public void handle(ActionEvent event) {
				String filePath = comboBox.getValue();
				displayFile(filePath);
			}		    
		});

		Scene scene = new Scene(new Group());
		scene.setRoot(border);
		
		String style = getClass().getResource("css/program.css").toExternalForm();
		scene.getStylesheets().add(style);

		primaryStage.setScene(scene);
		primaryStage.show();
	}
}
