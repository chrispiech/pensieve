package stanford.edu.gitviewer;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.IOUtils;

import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.scene.web.WebView;
import javafx.scene.input.ScrollEvent;

/**
 * A syntax highlighting code editor for JavaFX created by wrapping a
 * CodeMirror code editor in a WebView.
 * See http://codemirror.net for more information on using the codemirror editor.
 */
public class CodeEditor extends StackPane {
	// private static final String CPP_CODE = "x-c++src";
	private static final String JAVA_CODE = "x-java";

	/** a webview used to encapsulate the CodeMirror JavaScript. */
	private final WebView webview = new WebView();

	private Map<String, String> jsCache = new HashMap<String, String>();

	// Keeps track of the last scroll position so that you don't go
	// back to the top
	int scrollY;

	/**
	 * Create a new code editor.
	 * @param starterCode the initial code to be edited in the code editor.
	 */
	CodeEditor(String starterCode) {
		webview.setPrefWidth(600);
		URL cssUrl = getClass().getResource("css/codemirror.css");
		String cssPath = cssUrl.toExternalForm();
		webview.getEngine().setUserStyleSheetLocation(cssPath);
		this.getChildren().add(webview);
		webview.setOnScroll(new EventHandler<ScrollEvent>() {
			@Override 
			public void handle(ScrollEvent event) {
				scrollY = (Integer) webview.getEngine().executeScript("document.body.scrollTop;");
			}
		});
	}

	public void resetScroll() {
		scrollY = 0;
	}


	/** applies the editing template to the editing code to create the html+javascript source for a code editor. 
	 * @param code */
	private String applyEditingTemplate(String code) {
		String html = editingTemplate.replace("${code}", code);
		html = html.replace("${codemirrorjs}", loadCodeFromFile("codemirror.js"));
		html = html.replace("${clikejs}", loadCodeFromFile("clike.js"));
		html = html.replace("${scrollY}", ""+scrollY);
		return html;
	}

	/** returns code from a given file in viewable form. */
	private String loadCodeFromFile(String fileName) {
		if(jsCache.containsKey(fileName)) return jsCache.get(fileName);
		String resourceName = "js/" + fileName;
		InputStream input = getClass().getResourceAsStream(resourceName);
		StringWriter writer = new StringWriter();
		try {
			IOUtils.copy(input, writer, "UTF-8");
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		String js = writer.toString();
		String sourceTag = "<script>" + js + "</script>"; 
		return sourceTag;
	}

	/** returns the current code in the editor and updates an editing snapshot of the code which can be reverted to. */
	public String getCode() {
		return (String ) webview.getEngine().executeScript("editor.getValue();");
	}

	public void setCode(String code) {
		webview.getEngine().loadContent(applyEditingTemplate(code));

	}

	public WebView getView() {
		return webview;
	}


	/**
	 * a template for editing code - this can be changed to any template derived from the
	 * supported modes at http://codemirror.net to allow syntax highlighted editing of
	 * a wide variety of languages.
	 */
	private final String editingTemplate =
			"<!doctype html>" +
					"<html>" +
					"<head>" +
					"  ${codemirrorjs}" + 
					"  ${clikejs}"+
					"</head>" +
					"<body>" +
					"<form><textarea id=\"code\" name=\"code\">\n" +
					"${code}" +
					"</textarea></form>" +
					"<script>" +
					"  var editor = CodeMirror.fromTextArea(document.getElementById(\"code\"), {" +
					"    lineNumbers: false," +
					"    matchBrackets: true," +
					"    indentUnit:4," +
					"    lineWrapping: true," +
					"    indentWithTabs: true," +
					"    readOnly: true," +
					"    mode: \"text/" + JAVA_CODE +  "\"" +
					"  });" +
					"  window.onload = function(){window.scrollTo(0,${scrollY});};" +
					"</script>" +
					"</body>" +
					"</html>";

}