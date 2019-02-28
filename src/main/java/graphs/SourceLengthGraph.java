package graphs;


import java.util.List;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import stanford.edu.gitviewer.Intermediate;

public class SourceLengthGraph implements GitGraph{

	private static final double EPSILON = 0.000000001;

	final NumberAxis xAxis = new NumberAxis();
	final NumberAxis yAxis = new NumberAxis();
	final LineChart<Number,Number> lineChart = 
			new LineChart<Number,Number>(xAxis,yAxis);
	private Double maxY = null;
	private XYChart.Series<Number,Number> vertical;

	boolean disabled = false;

	public LineChart<Number,Number> getView() {
		lineChart.setAnimated(false);
		return lineChart;
	}

	public void drawGraph(List<Intermediate> history) {
		disabled = history.size() <= 1;
		if(disabled) return;
		maxY = null;
		lineChart.getData().clear();
		xAxis.setLabel("Time into Problem (hours)");
		yAxis.setLabel("Characters");

		Series<Number, Number> comments = getCommentSeries(history);
		Series<Number, Number> codes = getCodeSeries(history);
		addVerticalMarker();
		lineChart.getData().add(comments);
		lineChart.getData().add(codes);
	}
	
	public void setSelectedTime(double time) {
		if(!disabled) {
			setVerticalLocation(time);
		}
	}

	private void addVerticalMarker() {
		vertical = new Series<Number, Number>();
		vertical.setName("Selection");
		setVerticalLocation(0);
		lineChart.getData().add(vertical);
	}

	private Series<Number,Number> getCodeSeries(List<Intermediate> history) {
		XYChart.Series<Number,Number> codeSeries = new XYChart.Series<Number,Number>();
		codeSeries.setName("Code");
		for(Intermediate intermediate : history) {
			double x = intermediate.workingHours;
			if(intermediate.nonComments == 0) {
				continue;
			}
			double y = intermediate.nonComments;
			codeSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			if(maxY == null || y > maxY) maxY = y;
		}
		return codeSeries;
	}

	private XYChart.Series<Number,Number> getCommentSeries(List<Intermediate> history) {
		XYChart.Series<Number,Number> commentSeries = new XYChart.Series<Number,Number>();
		commentSeries.setName("Comments");
		for(Intermediate intermediate : history) {
			double x = intermediate.workingHours;
			if(intermediate.totalComments == 0) {
				continue;
			}
			double y = intermediate.totalComments;
			commentSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			if(maxY == null || y > maxY) maxY = y;
		}
		return commentSeries;
	}

	private void setVerticalLocation(double loc) {
		vertical.getData().clear();
		vertical.getData().add(new XYChart.Data<Number, Number>(loc, 0));
		vertical.getData().add(new XYChart.Data<Number, Number>(loc + EPSILON, maxY));
	}


}
