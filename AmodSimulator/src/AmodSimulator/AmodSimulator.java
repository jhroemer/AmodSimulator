package AmodSimulator;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;
import org.graphstream.ui.spriteManager.SpriteManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Scanner;

import static AmodSimulator.VehicleStatus.IDLE;

public class AmodSimulator {

    private static String styleSheetPath = "styles/style.css";
    private static String graphPath = "data/graphs/small-1.dgs";
    private static int timesteps = 100;
    static boolean IS_VISUAL = true;

    public static void main(String[] args) {

        Graph graph = parseGraph("test", graphPath);
        TripPlanner.init(graph);

        if (IS_VISUAL) {
            String styleSheet = parseStylesheet(styleSheetPath);
            graph.addAttribute("ui.stylesheet", styleSheet);
            graph.display();
        }

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", 1.0);
        }

        SpriteManager sman = new SpriteManager(graph);
        //AmodSprite s = new AmodSprite();
        Vehicle v = new Vehicle("test", graph.getNode("A"), sman.addSprite("testsprite", AmodSprite.class));
        Request r = new Request(1,graph.getNode("I"),graph.getNode("B"));
        v.addRequest(r);

        v.advance();
        for (int i = 0; i < timesteps; i++) {
            while (v.getStatus() != IDLE) {
                v.advance();
            }
            tick(graph);
            sleep(); //todo: How to make it sleep?
        }


    }

    /**
     *
     * @param graph
     */
    private static void tick(Graph graph) {
        //todo Everything that happens in each timestep
    }


    /**
     * Constructs a <Code>Graph</Code> from an dgs-file
     * @param fileId    Id to give the graph
     * @param filePath  Path to a file containing a graph in dgs-format
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
     * @param path      path to the CSS file
     * @return String   containing the stylesheet
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

    /**
     * Makes thread sleep
     */
    protected static void sleep() {
        try { Thread.sleep(5); } catch (Exception e) {}
    }
}
