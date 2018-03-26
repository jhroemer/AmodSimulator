package GraphCreator;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSinkDGS;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.*;

public class GeoGraph {

    private static String file = "fejoe";
    private static double latScale = 2;
    private static boolean print = false;
    private static boolean addHouses = false;
    private static boolean addHouseNames = false;
    private static boolean addCoast = true;


    private static String styleSheet =
            "node.house {" +
                    "	fill-color: black;" +
                    "	size: 5;" +
                    "	shape: box;" +
                    "}" +
                    "node.home {" +
                    "	fill-color: green;" +
                    "}" +
                    "node.coastRefPoint {" +
                    "	visibility-mode: hidden;" +
                    "}" +
                    "node.refPoint {" +
                    "	visibility-mode: hidden;" +
                    "}";


    public static void main(String args[]) {
        long start = System.currentTimeMillis();

        System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");

        Graph graph = new MultiGraph("Tutorial 1");

        parseOSM(graph);

        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.display().disableAutoLayout();

        System.out.println("Time taken (seconds) to make graph: " + (System.currentTimeMillis()-start)/1000);

        FileSinkDGS fs = new FileSinkDGS();
        try {
            fs.writeAll(graph,"data/DGSfiles/" + file + ".dgs");
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("Graph density: " + Toolkit.density(graph)); // TODO : good argument here, graph density for the real-world graph is very low
        System.out.println("Time taken (seconds) to make and save graph: " + (System.currentTimeMillis()-start)/1000);
    }

    /**
     * Parsing OSM
     * @param graph
     */
    public static void parseOSM(Graph graph) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File("data/osm/" + file + ".osm"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        Double minLat = null, minLon = null;
        Map<String, AbstractMap.SimpleEntry<Double,Double>> positions = new HashMap<>();

        String line = sc.nextLine().trim();

        while (minLat == null || minLon == null) {
            if (line.startsWith("<bounds")) {
                String[] lineSplit = line.split("\\s+");
                minLat = Double.parseDouble(lineSplit[1].substring(8,lineSplit[1].length()-1));
                minLon = Double.parseDouble(lineSplit[2].substring(8,lineSplit[2].length()-1));
            }
            line = sc.nextLine().trim();
        }

        if (print) System.out.printf("Minimum latitude  set to %f\nMinimum longitude set to %f\n\n", minLat, minLon);


        while (sc.hasNextLine()) {
            //general parsing of nodes, all nodes gets put on the map "positions"
            if (line.startsWith("<node")) {
                String[] lineSplit = line.split("\\s+");
                String id = null;
                Double lat = null,lon = null;
                for (String s : lineSplit) {
                    if (s.startsWith("id=")){
                        id = s.substring(4,s.length()-1);
                    }
                    if (s.startsWith("lat=")){
                        lat = Double.parseDouble(s.substring(5,s.length()-1));
                    }
                    if (s.startsWith("lon=")){
                        lon = Double.parseDouble(s.substring(5,s.length()-3));
                    }
                }
                if (id != null && lat != null && lon != null) {
                    positions.put(id,new AbstractMap.SimpleEntry<>(lat,lon));
                    if (positions.size()%1000000 == 0) System.out.println(positions.size()/1000000 + " million positions parsed");
                }


                //parsing of nodes with more than one line (so far only houses)
                if (!line.endsWith("/>")){
                    line = sc.nextLine().trim();
                    String no = null;
                    String street = null;
                    while (line.startsWith("<tag")) {
                        if (line.startsWith("<tag k=\"addr:housenumber\"")) no = line.substring(29,line.length()-3);
                        if (line.startsWith("<tag k=\"addr:street\"")) street = line.substring(24,line.length()-3);
                        line = sc.nextLine().trim();
                    }

                    /*
                    if (no != null && street != null && no.equals("18") && street.equals("Breumvej")) {
                        graph.addNode(id);
                        graph.getNode(id).addAttribute("xy", lon - minLon, (lat - minLat) * latScale);
                        graph.getNode(id).addAttribute("ui.class","home");
                        graph.getNode(id).addAttribute("ui.label", "Her boede Astrid");
                    }
                    */

                    if (addHouses) {
                        if (no != null && street != null) {
                            graph.addNode(id);
                            Node node = graph.getNode(id);

                            node.addAttribute("xy", lon - minLon, (lat - minLat) * latScale);
                            node.addAttribute("ui.class", "house");
                            if (addHouseNames) node.addAttribute("ui.label", street + " " + no);

                            if (print) System.out.printf("Adding node for %s %s\n", street, no);
                        }
                    }

                }

            }


            if (line.trim().startsWith("<way")) {
                boolean isWay = false;
                boolean isCoast = false;
                String name = null;

                //finding the id of the way
                String[] lineSplit = line.split("\\s+");
                String wayId = lineSplit[1].substring(4,lineSplit[1].length()-1);

                //counter to give the edges that creates the way different ids: wayId + - + counter
                int edgeCounter = 0;

                List<String> refPoints = new ArrayList<>();

                line = sc.nextLine().trim();


                //getting the id's of the reference points along the way
                while (line.startsWith("<nd ref")) {
                    refPoints.add(line.substring(9, line.length() - 3));
                    line = sc.nextLine().trim();
                }

                while (line.startsWith("<tag")) {
                    if (addCoast) if (line.startsWith("<tag k=\"natural\" v=\"coastline\"/>")) isCoast = true;
                    if (line.startsWith("<tag k=\"highway\"")) isWay = true;
                    if (line.startsWith("<tag k=\"name\"")) name = line.substring(17,line.length()-3);
                    line = sc.nextLine().trim();
                }


                Node lastNode = null;
                if (isWay || isCoast) {
                    if (print && isWay) System.out.println("Found way: " + name);
                    if (print && isCoast) System.out.println("Found coast: " + name);

                    for (String point : refPoints) {
                        AbstractMap.SimpleEntry position = positions.get(point);

                        //add the position to the graph as a node, if it does not already exists
                        if (graph.getNode(point) == null) {
                            graph.addNode(point);
                            graph.getNode(point).addAttribute("xy", ((Double) position.getValue()) - minLon, (((Double) position.getKey()) - minLat) * latScale);
                            if (isWay) graph.getNode(point).addAttribute("ui.class", "refPoint");
                            if (isCoast) graph.getNode(point).addAttribute("ui.class", "coastRefPoint");
                        }

                        Node node = graph.getNode(point);

                        //add edge from last point to this one
                        if (lastNode != null) {
                            graph.addEdge(wayId + "-" + edgeCounter++, lastNode, node);
                            //graph.getNode(point).addAttribute("ui.hide");
                            //System.out.println("Adding edge");
                        }
                        else node.addAttribute("ui.label", name + " starts");

                        lastNode = node;


                        //System.out.println(line);
                    }
                    lastNode.addAttribute("ui.label", name + " ends");
                }
            }
            line = sc.nextLine().trim();
        }

    }


    /**
     * Parsing house positions
     * @param graph
     */
    /*
    public static void parseHouses(Graph graph) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File("data/norddjurs2.osm"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            if (line.trim().startsWith("<node")) {
                if (line.endsWith("/>")) continue;
                //System.out.println(line);
                String[] lineSplit = line.split("\\s+");
                String id = null;
                Double lat = null,lon = null;
                for (String s : lineSplit) {
                    if (s.startsWith("id=")){
                        id = s.substring(4,s.length()-1);
                    }
                    if (s.startsWith("lat=")){
                        lat = Double.parseDouble(s.substring(5,s.length()-1));
                    }
                    if (s.startsWith("lon=")){
                        lon = Double.parseDouble(s.substring(5,s.length()-3));
                    }
                }
                line = sc.nextLine().trim();
                String no = null;
                String street = null;
                while (line.startsWith("<tag")) {
                    if (line.startsWith("<tag k=\"addr:housenumber\"")) no = line.substring(29,line.length()-3);
                    if (line.startsWith("<tag k=\"addr:street\"")) street = line.substring(24,line.length()-3);
                    line = sc.nextLine().trim();
                }
                //System.out.println(street);
                //System.out.println();
                if (id != null && lat != null && lon != null && no != null && street != null) {
                    graph.addNode(id);
                    Node node = graph.getNode(id);
                    node.addAttribute("xy",lon - 10.7289000, (lat - 56.4720000)*2);
                    node.addAttribute("ui.label", street + " " + no);
                }
            }
        }
    }
    */

    /**
     * Test of parsing danish cities
     */
    public static void parseDKcities(Graph graph) {
        Scanner sc = null;
        try {
            sc = new Scanner(new File("data/testData"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        //List<Node> nodes = new ArrayList<>();
        double minLat = 90;
        double minLong = 180;

        while (sc.hasNextLine()) {
            String line = sc.nextLine();
            String[] lineSplit = line.split("\\s+");
            String name = lineSplit[0].trim();

            String latString = lineSplit[1].trim();
            String[] latSplit = latString.split("[°']");
            double lat = Double.parseDouble(latSplit[0]) + Double.parseDouble(latSplit[1])*0.01;

            String longString = lineSplit[2].trim();
            String[] longSplit = longString.split("[°']");
            double longi = Double.parseDouble(longSplit[0]) + Double.parseDouble(longSplit[1])*0.01;

            if (lat < minLat) minLat = lat;
            if (longi < minLong) minLong = longi;

            graph.addNode(name);
            Node node = graph.getNode(name);

            node.addAttribute("layout.frozen");
            node.addAttribute("lat",lat);
            node.addAttribute("long",longi);
            node.addAttribute("ui.label", name);
        }

        for (Node n: graph) {
            double x = (double) n.getAttribute("long") - minLong;
            double y = (double) n.getAttribute("lat") - minLat;
            n.addAttribute("xy",x,y*2);
        }
    }
}
