package graphs;

import java.util.List;

import javafx.scene.chart.BarChart;
import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.chart.XYChart.Series;
import stanford.edu.gitviewer.Intermediate;

public class RunsGraph implements GitGraph{
	private static final double EPSILON = 0.000000001;

	final NumberAxis xAxis = new NumberAxis();
	final NumberAxis yAxis = new NumberAxis();
	final LineChart<Number,Number> lineChart = 
			new LineChart<Number,Number>(xAxis,yAxis);

	private Double maxY = null;
	private XYChart.Series<Number,Number> vertical;
	
	boolean disabled = false;

	@Override
	public Chart getView() {
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
		yAxis.setLabel("Times Run");

		Series<Number, Number> series = getIndentSeries(history);
		if(series != null) {
			addVerticalMarker();
			lineChart.getData().add(series);
		}
	}

	@Override
	public void setSelectedTime(double time) {
		if(vertical != null && !disabled) {
			setVerticalLocation(time);
		}
	}

	private Series<Number, Number> getIndentSeries(List<Intermediate> history) {

		double rate = 10.0/60.0;
		
		Intermediate last = history.get(history.size() - 1);
		double maxX = last.workingHours;
		int nBuckets = (int) Math.ceil(maxX / rate) + 1;
		if(nBuckets == 0) return null;
		int[] runBuckets = new int[nBuckets];

		// only has a point every 15 mins for visual clarity
		// first collect the histogram
		for(Intermediate intermediate : history) {
			int index = (int) (intermediate.workingHours / rate)+ 1;
			if(index < nBuckets && index >= 0) {
				runBuckets[index] += intermediate.deltaRuns;
			}
		}

		XYChart.Series<Number,Number> codeSeries = new XYChart.Series<Number,Number>();
		codeSeries.setName("Times Run");
		for(int i = 0; i < nBuckets; i++) {
			double x = i * rate;
			double y = runBuckets[i];
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
