package AmodSimulator;

import SCRAM.*;
import org.graphstream.graph.Graph;

import java.io.*;
import java.util.*;

import static AmodSimulator.AmodSimulator.PRINT;
import static AmodSimulator.ExperimentRunner.getGraphTypes;

public class Utility {

    /**
     * Generater vehicles and places them at random nodes in the graph
     * @param graph
     * @param numVehicles
     * @return
     */
    public static List<Vehicle> generateVehicles(Graph graph, int numVehicles) {
        List<Vehicle> vehicles = new ArrayList<Vehicle>();
        Random r = new Random();
        for (int i = 0; i < numVehicles; i++) {
            vehicles.add(new Vehicle("v" + i, graph.getNode(r.nextInt(graph.getNodeCount()))));
        }
        return vehicles;
    }

    /**
     * Method that parses a stylesheet to use with the graph
     * @param path      path to the CSS file
     * @return String   containing the stylesheet
     */
    public static String parseStylesheet(String path) {
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

    // fixme: is this still needed? the only thing I use is IndexSCRAM..
    static List<Edge> assign(AssignmentType type, List<Vehicle> vehicles, List<Request> requests, int timeStep) {

        // done to avoid major and annoying generic refactoring in AmodSimulator - adds some (linear) running time
        List<Node> vehicleNodeList = new ArrayList<>(vehicles);
        List<Node> requestNodeList = new ArrayList<>(requests);

        switch (type) {
            case SCHWACHSINN:
                return schwachsinnAssign(vehicleNodeList, requestNodeList, timeStep);
            case HUNGARIAN:
//                return hungarianAssign(vehicles,requests);
            case ObjectSCRAM:
                SCRAM scram = new SCRAM(vehicleNodeList, requestNodeList, timeStep);
//                System.out.println("SCRAM TOOK: " + (System.currentTimeMillis() - start) + " ms");
                return scram.getAssignments();
            case IndexSCRAM:
//                long start = System.currentTimeMillis();
                IndexBasedSCRAM idxScram = new IndexBasedSCRAM(vehicleNodeList, requestNodeList, timeStep);
//                System.out.println("SCRAM TOOK: " + (System.currentTimeMillis() - start) + " ms");
                return idxScram.getAssignments();
        }

        try {
            throw new Exception(type + " is not a valid assignment method");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return new ArrayList<>();
    }

    /**
     *
     * @param vehicles
     * @param requests
     * @param timeStep
     * @return
     */
    private static List<Edge> schwachsinnAssign(List<Node> vehicles, List<Node> requests, int timeStep) {

        List<Edge> assignments = new ArrayList<>();

        int numToAssign = Math.min(vehicles.size(),requests.size());

        if (PRINT && numToAssign != 0) System.out.println("\nAssigning");

        for (int i = 0; i < numToAssign; i++) {
            assignments.add(new Edge(vehicles.get(i), requests.get(i), timeStep));
            if (PRINT) System.out.println("\tVehicle "+ vehicles.get(i).getInfo() + " <-- request " + requests.get(i).getInfo());
        }

        return assignments;
    }

    /**
     *
     * @param origin
     * @param destination
     * @return
     */
    public static int getDist(org.graphstream.graph.Node origin, org.graphstream.graph.Node destination) {
        return origin.getAttribute("distTo"+ destination.getId());
    }

    /**
     *
     * @param graph
     */
    public static void printDistances(Graph graph) {
        int n = graph.getNodeCount();
        System.out.print("\t\t");
        for (int j = 0; j < n; j++) System.out.print(graph.getNode(j).getId() + "\t");
        System.out.println();
        for (int i = 0; i < n; i++) {
            System.out.print(i + "(" + graph.getNode(i).getId() + ") :");
            for (int j = 0; j < n; j++) {
                System.out.print("\t" + Utility.getDist(graph.getNode(i), graph.getNode(j)));
            }
            System.out.println();
        }
    }

    // FIXME: ready to be deleted?
    private static void addDummyNodes(org.jgrapht.Graph<Node, Assignment> graph, Set<Node> vehicles, Set<Node> requests) {

        int numVeh = vehicles.size();
        int numReq = requests.size();
        if (numVeh == numReq) return; //no need for dummy nodes

        //making dummies
        int numDummies = Math.abs(numVeh - numReq);
        Set<Node> dummies = new HashSet<>();
        for (int i = 0; i < numDummies; i++) {
            DummyNode dummy = new DummyNode();
            dummies.add(dummy);
            graph.addVertex(dummy);
        }

        //setting edges from dummies to the vehicles/request in the biggest of the Sets
        Set<Node> smallest = (numVeh < numReq)? vehicles : requests;
        Set<Node> biggest = (numVeh > numReq)? vehicles : requests;

        for (Node dummy : dummies) {
            for (Node real : biggest) {
                Assignment assignmentEdge = graph.addEdge(dummy,real); //assignments containing a dummy does not contain either a vehicle or a request
                assignmentEdge.setToDummy();
                graph.setEdgeWeight(assignmentEdge, Integer.MAX_VALUE);
            }
        }

        //adding the dummies to the smallest of the Sets
        smallest.addAll(dummies);

    }

    /**
     *
     * @return
     * @param path
     */
    public static Properties loadProps(String path) {
        Properties props = new ExpProperties();
        InputStream in = null;
        try {
            in = new FileInputStream(path);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }

        if (in != null) try {
            props.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return props;
    }

    /**
     *
     * @param props
     */
    public static void saveResultsAsFile(Properties props) {
        File file = new File(props.getProperty("resultFolder") + props.getProperty("name"));
        try {
            props.store(new FileOutputStream(file), props.getProperty("name"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Deprecated // todo : replace w. Math.sqrt(calculateVarianceOfProp())
    /**
     *
     * @param unoccupiedPercentageList
     * @param avg
     * @return
     */
    public static double calculateStandardDeviation(List<Double> unoccupiedPercentageList, double avg) {
        double variance = 0.0;
        for (Double d : unoccupiedPercentageList) {
            double difference = d - avg;
            variance += difference * difference;
        }
        variance = variance / (double) unoccupiedPercentageList.size();
        return Math.sqrt(variance);
    }

    /**
     * Calculates the variance of a property
     *
     * @param props
     * @param graphType
     * @param property
     * @param avg
     */
    public static double calculateVarianceOfProp(Properties props, String graphType, String property, double avg) {
        List<Double> list = new ArrayList<>();

        for (int i = 0; i < Integer.valueOf(props.getProperty("trials")); i++) {
            String key = graphType + "_" + i + "_" + property;
            list.add(Double.valueOf(props.getProperty(key)));
        }

        double variance = 0.0;
        for (Double d : list) {
            double difference = d - avg;
            variance += difference * difference;
        }
        return variance / (double) list.size();
    }

    /**
     *
     * @param props
     * @param propsKey
     * @return
     */
    public static Map<Integer, Integer> parseIntIntMap(Properties props, String propsKey) {
        Map<Integer, Integer> map = new TreeMap<>();
        String[] values = props.getProperty(propsKey).replaceAll("\\{", "").replaceAll("}", "").replaceAll(" ", "").split(",");
        for (String s : values) {
            String[] splitValues = s.split("=");
            map.put(Integer.valueOf(splitValues[0]), Integer.valueOf(splitValues[1]));
        }
        return map;
    }

    /**
     *
     * @param props
     * @param propsKey
     * @return
     */
    public static Map<Integer,Double> parseIntDoubleMap(Properties props, String propsKey) {
        Map<Integer, Double> map = new TreeMap<>();
        String[] values = props.getProperty(propsKey).replaceAll("\\{", "").replaceAll("}", "").replaceAll(" ", "").split(",");
        for (String s : values) {
            String[] splitValues = s.split("=");
            map.put(Integer.valueOf(splitValues[0]), Double.valueOf(splitValues[1]));
        }
        return map;
    }

    /**
     * Parses experiment results (from props) and formats and outputs the latex-avgWaitingTimes-plots for the report.
     *  @param props
     * @param graphTypes
     */
    public static void updatePlots(Properties props, String[] graphTypes) {
        for (String graphType : graphTypes) {

            Map<Integer, Double> avgWaitingTimes = parseIntDoubleMap(props, "TOTAL_" + graphType + "_avgWaitingTimes");
            Map<Integer, Double> stdDeviationMap = parseIntDoubleMap(props, "TOTAL_" + graphType + "_waitingTimeStdDev");

            StringBuilder s = new StringBuilder();
            s.append("\\begin{tikzpicture}\n" +
                    "\\begin{axis}[\n" +
                    "    ybar,\n" +
                    "    ylabel={\\# Passengers Waiting},\n" +
                    "    xlabel={Minutes waiting},\n" +
                    "    symbolic x coords={0,5,10,15,20,25,30,35,40,45,50,55,60,65,70,75,80},\n" +
                    "    xtick=data,\n" +
                    "    ymin=0,\n" +
                    "    xmin=0,\n" +
                    "    enlarge x limits=0.04,\n" +
                    "    bar width=7pt,\n" +
                    "    %width=0.5\\textwidth,\n" +
                    "    scale only axis,\n" +
                    "    ]\n" +
                    "\\addplot[draw=blue, pattern=horizontal lines light blue, error bars/.cd, y dir=both, y explicit] coordinates {");


            for (Integer interval : avgWaitingTimes.keySet()) {
                // The avg. waiting time for interval, e.g: (5, 2835.6)
                s.append("(").append(interval).append(",").append(avgWaitingTimes.get(interval)).append(")");
                // The std. dev. for that interval, e.g: +- (0.3515,0.3515)
                s.append(" +- (").append(stdDeviationMap.get(interval)).append(",").append(stdDeviationMap.get(interval)).append(") ");
            }
            s.append("};\n" +
                    "\\end{axis}\n" +
                    "\\end{tikzpicture}");

            String chapter = props.getProperty("figuresFolder");
            String path = chapter + "/" + graphType + "WaitingTimes.tex";

            BufferedWriter writer = null;
            try {
                writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path), "UTF-8"));
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.write(s.toString());
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
//        Map<Integer, Double> map = new TreeMap<>();
//        map.put(1, 1.0);
//        map.put(2, 2.0);
//        System.out.println(map);
//        String[] values = map.toString().replaceAll("\\{", "").replaceAll("}", "").replaceAll(" ", "").split(",");
//        System.out.println(values);
//        System.exit(1);


        Properties props = loadProps("data/experimentResults/chapter4.properties");
//        Map<Integer, Integer> map = parseIntIntMap(props, "BANANATREE", 2);
//        System.out.println(map);
        String[] graphTypes = getGraphTypes(props.getProperty("graphDir"));
        int vehicleSpeed = Integer.valueOf(props.getProperty("vehicleSpeed"));
        updatePlots(props, graphTypes);
    }
}
