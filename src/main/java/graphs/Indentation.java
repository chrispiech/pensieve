package graphs;

import java.util.List;

import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import stanford.edu.gitviewer.Intermediate;

public class Indentation implements GitGraph{
	private static final double EPSILON = 0.000000001;

	final NumberAxis xAxis = new NumberAxis();
	final NumberAxis yAxis = new NumberAxis();
	final LineChart<Number,Number> lineChart = 
			new LineChart<Number,Number>(xAxis,yAxis);

	private Double maxY = null;
	private XYChart.Series<Number,Number> vertical;
	
	boolean disabled = false;

	@Override
	public LineChart<Number, Number> getView() {
		lineChart.setAnimated(false);
		return lineChart;
	}

	@Override
	public void drawGraph(List<Intermediate> history) {
		disabled = history.size() <= 1;
		if(disabled) return;
		
		maxY = null;
		lineChart.getData().clear();


		xAxis.setLabel("Time into Problem (hours)");
		yAxis.setLabel("Indendation Errors");


		Series<Number, Number> series = getIndentSeries(history);
		addVerticalMarker();
		lineChart.getData().add(series);

	}

	@Override
	public void setSelectedTime(double time) {
		if(vertical != null && !disabled) {
			setVerticalLocation(time);
		}
	}

	private Series<Number, Number> getIndentSeries(List<Intermediate> history) {

		XYChart.Series<Number,Number> codeSeries = new XYChart.Series<Number,Number>();
		codeSeries.setName("Indentation");
		for(Intermediate intermediate : history) {
			double x = intermediate.workingHours;

			double y = intermediate.indentationErrors;
			codeSeries.getData().add(new XYChart.Data<Number, Number>(x, y));
			if(maxY == null || y > maxY) maxY = y;
		}
		return codeSeries;

	}

	private void addVerticalMarker() {
		vertical = new Series<Number, Number>();
		vertical.setName("Selection");
		setVerticalLocation(0);
		lineChart.getData().add(vertical);
	}

	private void setVerticalLocation(double loc) {
		vertical.getData().clear();
		vertical.getData().add(new XYChart.Data<Number, Number>(loc, 0));
		vertical.getData().add(new XYChart.Data<Number, Number>(loc + EPSILON, maxY));
	}

}
