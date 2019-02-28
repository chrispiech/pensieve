package graphs;

import java.util.List;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import stanford.edu.gitviewer.Intermediate;

public class EmptyGraph implements GitGraph{

	final NumberAxis xAxis = new NumberAxis();
    final NumberAxis yAxis = new NumberAxis();
	final LineChart<Number,Number> lineChart = 
            new LineChart<Number,Number>(xAxis,yAxis);
	
	@Override
	public LineChart<Number, Number> getView() {
		lineChart.setAnimated(false);
		return lineChart;
	}

	@Override
	public void drawGraph(List<Intermediate> history) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelectedTime(double workingHours) {
		// TODO Auto-generated method stub
		
	}
	
	
}
