package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

public class AmodSimulator {

    private static String styleSheetPath = "styles/style.css";
    private static String graphPath = "data/graphs/small-1.dgs";
     static boolean IS_VISUAL = false;

    public static void main(String[] args) {
        String styleSheet = parseStylesheet(styleSheetPath);

        Graph graph = parseGraph(graphPath);
        graph.display();
        graph.addAttribute("ui.stylesheet", styleSheet);

        //todo:
        /*
        for (i < timestep) {
            tick(graph);
            sleep();
        }
        */


        /*
        Graph g = new MultiGraph("test");
        FileSource fs = new FileSourceDGS() {
        };

        fs.addSink(g);

        try {
            fs.readAll(graphPath);
        } catch( IOException e) {
        } finally {
            fs.removeSink(g);
        }

        graph.display();
        */

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
