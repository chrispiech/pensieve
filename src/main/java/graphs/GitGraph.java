package graphs;

import java.util.List;

import javafx.scene.chart.Chart;
import javafx.scene.chart.LineChart;
import stanford.edu.gitviewer.Intermediate;

public interface GitGraph {
	
	public Chart getView();
	
	public void drawGraph(List<Intermediate> history);

	public void setSelectedTime(double workingHours);
}
