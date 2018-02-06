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

public class AmodSimulator {

    private static String styleSheetPath = "styles/style.css";
    private static String graphPath = "data/graphs/small-1.dgs";
    private static int timesteps = 10000000;
    static boolean IS_VISUAL = true;

    public static void main(String[] args) {

        Graph graph = parseGraph("test", graphPath);
        TripPlanner.init(graph);
        graph.display();
        SpriteManager sman = new SpriteManager(graph);

        if (IS_VISUAL) {
            String styleSheet = parseStylesheet(styleSheetPath);
            graph.addAttribute("ui.stylesheet", styleSheet);
        }

        for (Edge e : graph.getEdgeSet()) {
            e.setAttribute("layout.weight", 1.0);
        }

        AmodSprite s = sman.addSprite("s1", AmodSprite.class);
        AmodSprite s2 = sman.addSprite("s2", AmodSprite.class);
        Vehicle v1 = new Vehicle("v1", graph.getNode("A"), s); //sman.addSprite("s1", AmodSprite.class));
        Request r1 = new Request(1, graph.getNode("I"), graph.getNode("D"));
        Request r2 = new Request(2, graph.getNode("C"), graph.getNode("G"));
        v1.addRequest(r1);
        Vehicle v2 = new Vehicle("v2", graph.getNode("B"), s2);
        v2.addRequest(r2);

        for (int j = 0; j < 50; j++) sleep();

        for (int i = 0; i < timesteps; i++) {
            tick(graph);
            v1.advance();
            v2.advance();
            if (v1.getCurrentRequest() == null || v2.getCurrentRequest() == null) break;

            //System.out.println("Vehicle is on edge: " + v1.getCurrentEdge().getId());
            //System.out.println("Sprite is on edge:  " + s.getAttachment().getId() + " \n");
            //if (!v1.getCurrentEdge().getId().equals(s.getAttachment().getId())) {
            //    System.out.println("--- Vehicle and Sprite departed");
            //    System.exit(0);
            //}
            sleep();
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
        try { Thread.sleep(50); } catch (Exception e) {}
    }
}
