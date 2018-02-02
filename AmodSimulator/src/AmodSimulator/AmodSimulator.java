package AmodSimulator;

import org.graphstream.graph.Graph;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class AmodSimulator {

    private static String styleSheetPath = "styles/style.css";
    private static String graphPath = "small-1.dgs";
    private static boolean IS_VISUAL = false;

    public static void main(String[] args) {
        String styleSheet = parseStylesheet(styleSheetPath);

        Graph graph = parseGraph(graphPath);
        graph.display();
        graph.addAttribute("ui.stylesheet", styleSheet);

        //todo runSimulation(graph)

    }

    public static Graph parseGraph(String file) {
        //TODO construct graph from dgs file
        return null;
    }

    /**
     * Method that parses a stylesheet to use with the graph
     * @param path path to the CSS file
     * @return String containing the stylesheet
     */
    private static String parseStylesheet(String path) {
        String styleSheet = "";
        File file = new File(path);
        Scanner sc = null;
        try {
            sc = new Scanner(file);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        if (sc != null) {
            sc.useDelimiter("\\Z");
            styleSheet = sc.next();
        } else {
            System.out.println("something with parsing went wrong");
        }
        if (styleSheet.equals("")) System.out.println("No stylesheet made"); //Todo make exception instead
        return styleSheet;
    }
}
