package graphs;

import java.util.ArrayList;
import java.util.List;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.geometry.Pos;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.control.ComboBox;
import javafx.scene.layout.VBox;
import stanford.edu.gitviewer.FileHistory;
import stanford.edu.gitviewer.Intermediate;

public class GraphChooser{

	VBox graphChoserBox = null;
	GitGraph currGraph = null;
	ComboBox<String> comboBox = null;
	
	List<Intermediate> historyCache = null;
	double selectedTimeCache = 0;

	public GraphChooser(String graphType) {
		graphChoserBox = new VBox();
		comboBox = new ComboBox<String>();
		populateComboBox();
		setGraphType(graphType);
		comboBox.setValue(graphType);
		graphChoserBox.getChildren().add(comboBox);
		graphChoserBox.getChildren().add(currGraph.getView());
		graphChoserBox.setAlignment(Pos.CENTER);
		
		// toggle which workflow graph is displayed
		comboBox.setOnAction(new EventHandler<ActionEvent>() {
			public void handle(ActionEvent event) {
				String graphType = comboBox.getValue();
				setGraphType(graphType);
				graphChoserBox.getChildren().remove(1);
				graphChoserBox.getChildren().add(1, currGraph.getView());
				if(historyCache != null) {
					currGraph.drawGraph(historyCache);
					currGraph.setSelectedTime(selectedTimeCache);
				}
			}		    
		});
	}

	private void setGraphType(String graphType) {
		if(graphType.equals("SourceLength")) { 
			currGraph = new SourceLengthGraph();
		} else if(graphType.equals("Indentation")) {
			currGraph = new Indentation();
		} else if(graphType.equals("None")) {
			currGraph = new EmptyGraph();
		} else if(graphType.equals("Runs")) {
			currGraph = new RunsGraph();
		} else {
			throw new IllegalArgumentException("Invalid graph: " + graphType);
		}
	}

	public VBox getView() {
		return graphChoserBox;
	}

	public void drawGraph(List<Intermediate> history) {
		historyCache = history;
		currGraph.drawGraph(history);
	}

	public void setSelectedTime(double workingHours) {
		selectedTimeCache = workingHours;
		currGraph.setSelectedTime(workingHours);
	}
	
	private void populateComboBox() {
		List<String> graphTypes = new ArrayList<String>();
		graphTypes.add("SourceLength");
		graphTypes.add("Runs");
		graphTypes.add("Indentation");
		graphTypes.add("None");
		ObservableList<String> options = 
				FXCollections.observableArrayList(graphTypes);
		comboBox.setItems(options);
	}


}
