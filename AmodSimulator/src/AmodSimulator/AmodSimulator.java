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

        Graph graph = parseGraph("test", graphPath);
        graph.addAttribute("ui.stylesheet", styleSheet);


        //todo:
        /*
        for (i < timestep) {
            tick(graph);
            sleep();
        }
        */

        graph.display();




    }

    /**
     * Constructs a <Code>Graph</Code> from an dgs-file
     * @param fileId Id to give the graph
     * @param filePath path to a file containing a graph in dgs-format
     * @return
     */
    public static Graph parseGraph(String fileId, String filePath) {
        Graph graph = new MultiGraph(fileId);
        FileSource fs = new FileSourceDGS() {
        };

        fs.addSink(graph);

        try {
            fs.readAll(filePath);
        } catch( IOException e) {
        } finally {
            fs.removeSink(graph);
        }

        return graph;
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
