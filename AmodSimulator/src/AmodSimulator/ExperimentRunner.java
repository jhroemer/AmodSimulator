package AmodSimulator;

import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.file.FileSource;
import org.graphstream.stream.file.FileSourceDGS;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

import static AmodSimulator.Utility.parseIntDoubleMap;
import static AmodSimulator.Utility.parseIntIntMap;

/**
 * Class used to run experiments.
 * Uses java Properties files.
 */
public class ExperimentRunner {

    /**
     *
     * @param args should be a path to a folder containing properties for experiments
     */
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Please provide the path for a properties file folder as argument");
            System.exit(1);
        }
        if (args[0].equals("predefined")) {
            System.out.println("running predefined - but still nothing to run");
            // Graph randomGraph = RandomGraphGenerator.countrysideGraph();
            // runPredefinedExperiment(graph, 1000, true);
            System.exit(1);
        }

        List<Properties> propertiesList = getPropertiesFromFolder(args[0]);
        System.out.println(propertiesList);
        for (Properties props : propertiesList) {
            System.out.println("running experiment: " + props.getProperty("name"));
            runExperiment(props);
        }
    }

    /**
     *
     * @param props the properties object that holds all the info the experiment needs
     */
    private static void runExperiment(Properties props) {
        int numVehicles = Integer.parseInt(props.getProperty("numVehicles"));
        int trials = Integer.parseInt(props.getProperty("trials"));
        int timeSteps = Integer.parseInt(props.getProperty("timeSteps"));
        double lambda = Double.parseDouble(props.getProperty("requestsPerDay")) / 288.0; // there are 288 5min intervals per day
        int vehicleSpeed = Integer.parseInt(props.getProperty("vehicleSpeed"));
        boolean visual = Boolean.parseBoolean(props.getProperty("isVisual"));
        String[] graphTypes = getGraphTypes(props.getProperty("graphDir"));
        ExtensionType extensionType = ExtensionType.valueOf(props.getProperty("extension"));

        // TODO: perform warm-up to figure out correct vehicle-request relationship?

        // for each graph-type, do 50 trials on 5 random instances of the graph-type
        for (String graphType : graphTypes) {
            List<Graph> graphList = getGraphsFromFolder(props.getProperty("graphDir") + "/" + graphType);
            System.out.println();
            System.out.println("starting trials on graph type: " + graphType);

            Map<Integer, Integer> totalWaitMap = new TreeMap<>();
            for (int j = 0; j < 14; j++) totalWaitMap.put(j * vehicleSpeed, 0);
            Map<Integer, Double> totalWaitPercentageMap = new TreeMap<>();
            for (int j = 0; j < 14; j++) totalWaitPercentageMap.put(j * vehicleSpeed, 0.0);

            long start = System.currentTimeMillis();
            for (int i = 0; i < trials; i++) {
                System.out.println("starting trial: " + i);
                Graph graph = getCorrectGraph(i, graphList); // 10 trials per graph, 0-9 graph 1, 10-19 graph2 etc..

                ////////// running the simulation //////////
                AmodSimulator simulator = new AmodSimulator(graph, visual, numVehicles, extensionType, lambda);
                if (visual) sleep(2500); //Makes the simulation start after the graph is drawn.
                for (int j = 0; j < timeSteps; j++) {
                    simulator.tick(graph, j);
                    if (visual) sleep(50);
                }
                System.out.println("Unassigned: " + simulator.getRequests().size());
                ////////// simulation done //////////

                collectTrialResults(simulator, props, graphType, numVehicles, vehicleSpeed, i, totalWaitMap, totalWaitPercentageMap);
            }
            System.out.println("one graph took: " + (System.currentTimeMillis() - start) + " ms");
            collectTotalResults(props, trials, totalWaitMap, graphType, vehicleSpeed, totalWaitPercentageMap);
        }

        Utility.saveResultsAsFile(props);
        Utility.updatePlots(props, graphTypes);
    }

    /**
     * Collects result for the i'th trial
     *  @param simulator
     * @param props
     * @param graphType
     * @param numVehicles
     * @param i
     * @param totalWaitMap
     * @param totalWaitPercentageMap
     */
    private static void collectTrialResults(AmodSimulator simulator, Properties props, String graphType, int numVehicles, int vehicleSpeed, int i, Map<Integer, Integer> totalWaitMap, Map<Integer, Double> totalWaitPercentageMap) {
        // waiting time returns no. of 5 min-interval ticks - divide by five to get minutes
        double avgWait = ((double) simulator.getWaitingTime() / 5) / (double) simulator.getAssignedRequests().size();
        double waitVariance = simulator.getWaitVariance(avgWait) * vehicleSpeed; // todo: use the Utility method instead?

        props.setProperty(graphType + "_" + i + "_unoccupied", String.valueOf(simulator.getUnoccupiedKmDriven()));
        props.setProperty(graphType + "_" + i + "_avgUnoccupied", String.valueOf((double) simulator.getUnoccupiedKmDriven() / (double) numVehicles));
        props.setProperty(graphType + "_" + i + "_unoccupiedPercentage", String.valueOf(simulator.getAvgUnoccupiedPercentage()));
        props.setProperty(graphType + "_" + i + "_wait", String.valueOf(simulator.getWaitingTime()));
        props.setProperty(graphType + "_" + i + "_avgWait", String.valueOf(avgWait));
        props.setProperty(graphType + "_" + i + "_avgIdleVehicles", String.valueOf(simulator.getAverageIdleVehicles())); // FIXME : will not work in extensions
        props.setProperty(graphType + "_" + i + "_unservedRequests", String.valueOf(simulator.getUnservedRequests().size()));
        props.setProperty(graphType + "_" + i + "_waitVariance", String.valueOf(waitVariance));
        props.setProperty(graphType + "_" + i + "_waitStdDev", String.valueOf(Math.sqrt(waitVariance)));

        Map<Integer, Integer> waitMap = new TreeMap<>();

        for (int j = 0; j < 14; j++) waitMap.put(j * vehicleSpeed, 0); // zero-entries have to be included

        for (Request r : simulator.getAssignedRequests()) {
            // waiting times for i´th trial
            if (waitMap.containsKey(r.getWaitTime() * vehicleSpeed)) {
                int number = waitMap.get(r.getWaitTime() * vehicleSpeed) + 1;
                waitMap.put(r.getWaitTime() * vehicleSpeed, number);
            }
            else waitMap.put(r.getWaitTime() * vehicleSpeed, 1);

            // waiting times in total
            if (totalWaitMap.containsKey(r.getWaitTime() * vehicleSpeed)) {
                int number = totalWaitMap.get(r.getWaitTime() * vehicleSpeed) + 1;
                totalWaitMap.put(r.getWaitTime() * vehicleSpeed, number);
            }
            else totalWaitMap.put(r.getWaitTime() * vehicleSpeed, 1);
        }

        // find out the percentage-wise distribution of requests within the different intervals
        Map<Integer, Double> waitPercentageMap = new TreeMap<>();
        for (Integer interval : waitMap.keySet()) {
            int numRequests = waitMap.getOrDefault(interval, 0);
            double inPercentage = ((double) numRequests / (double) simulator.getAssignedRequests().size()) * 100;
            waitPercentageMap.put(interval, inPercentage);

            // adding to the totals
            double total = totalWaitPercentageMap.getOrDefault(interval, 0.0);
            totalWaitPercentageMap.put(interval, total += inPercentage);
        }
        props.setProperty(graphType + "_" + i + "_waitingTimesPercentage", String.valueOf(waitPercentageMap));
        props.setProperty(graphType + "_" + i + "_waitingTimes", String.valueOf(waitMap));
    }

    /**
     *
     *  @param props
     * @param trials
     * @param totalWaitMap
     * @param graphType
     * @param vehicleSpeed
     * @param totalWaitPercentageMap
     */
    private static void collectTotalResults(Properties props, int trials, Map<Integer, Integer> totalWaitMap, String graphType, int vehicleSpeed, Map<Integer, Double> totalWaitPercentageMap) {
//        getTotalOfProp(props, graphType, "waitVariance");
        double totalUnoccupiedPercentage = getTotalOfProp(props, graphType, "unoccupiedPercentage");

        props.setProperty("TOTAL_" + graphType + "_unoccupied", String.valueOf(getTotalOfProp(props, graphType, "unoccupied")));
        props.setProperty("TOTAL_" + graphType + "_avgUnoccupied", String.valueOf(getTotalOfProp(props, graphType, "avgUnoccupied") / (double) trials));
        props.setProperty("TOTAL_" + graphType + "_avgUnoccupiedPercentage", String.valueOf(totalUnoccupiedPercentage / (double) trials));
        props.setProperty("TOTAL_" + graphType + "_wait", String.valueOf(getTotalOfProp(props, graphType, "wait")));
        props.setProperty("TOTAL_" + graphType + "_avgWait", String.valueOf(getTotalOfProp(props, graphType, "avgWait") / (double) trials));
        props.setProperty("TOTAL_" + graphType + "_avgIdleVehicles", String.valueOf(getTotalOfProp(props, graphType, "avgIdleVehicles") / (double) trials));
        props.setProperty("TOTAL_" + graphType + "_avgWaitVariance", String.valueOf(getTotalOfProp(props, graphType, "waitVariance") / (double) trials));

        // get averages of waiting times
        Map<Integer, Double> avgWaitingTimes = new TreeMap<>();
        for (Integer num : totalWaitMap.keySet()) {
            double avgOverTrials = totalWaitMap.get(num) / (double) trials;
            avgWaitingTimes.put(num, avgOverTrials);
        }

        // fixme: normalizing the added total values to percentage
        for (Integer interval : totalWaitPercentageMap.keySet()) {
            double avgOverTrials = totalWaitPercentageMap.get(interval) / (double) trials;
            totalWaitPercentageMap.put(interval, avgOverTrials);
        }
        props.setProperty("TOTAL_" + graphType + "_avgWaitingTimesPercentage", String.valueOf(totalWaitPercentageMap));


        double unOccupiedVariance = Utility.calculateVarianceOfProp(props, graphType, "unoccupiedPercentage", totalUnoccupiedPercentage / (double) trials);
        double unoccupiedStdDev = Math.sqrt(unOccupiedVariance);

        // props.setProperty("TOTAL_" + graphType + "_avgWaitingTimes", String.valueOf(waitingTimes));
        props.setProperty("TOTAL_" + graphType + "_avgWaitingTimes", String.valueOf(avgWaitingTimes));
        props.setProperty("TOTAL_" + graphType + "_stdDevUnoccupied", String.valueOf(unoccupiedStdDev));

        calcWaitingTimesStdDev(props, trials, graphType, totalWaitPercentageMap);
    }

    /**
     *  @param props
     * @param trials
     * @param graphType
     * @param totalWaitPercentageMap
     */
    private static void calcWaitingTimesStdDev(Properties props, int trials, String graphType, Map<Integer, Double> totalWaitPercentageMap) {
        // get the map w. the averages FIXME: shouldn't this also just be given as param from outside the method?
        String propsKey = "TOTAL_" + graphType + "_avgWaitingTimes";
        Map<Integer, Double> avgWaitingTimes = Utility.parseIntDoubleMap(props, propsKey);

        // get the maps w. the trial results
        List<Map<Integer, Integer>> mapList = new ArrayList<>();
        List<Map<Integer, Double>> mapListPercentage = new ArrayList<>();
        for (int i = 0; i < trials; i++) {
            mapList.add(parseIntIntMap(props, graphType + "_" + i + "_waitingTimes"));
            mapListPercentage.add(parseIntDoubleMap(props, graphType + "_" + i + "_waitingTimesPercentage"));
        }

        // map to hold std. dev. values per time interval
        Map<Integer, Double> stdDevMap = new TreeMap<>();
        Map<Integer, Double> stdDevMapPercentage = new TreeMap<>();

        // for each waiting interval, calc. std. dev.
        for (Integer interval : avgWaitingTimes.keySet()) {
            // this should loop over the 5-minute intervals - 0, 5, 10, 15.. etc.
            double variance = 0.0;
            for (Map<Integer, Integer> map : mapList) {
                // find difference from avg.
                double difference = (double) map.getOrDefault(interval, 0) - avgWaitingTimes.get(interval);
                variance += difference * difference;
            }
            variance = variance / (double) trials;
            stdDevMap.put(interval, Math.sqrt(variance));
        }

        // fixme: for the new percentage-thingy
        for (Integer interval : totalWaitPercentageMap.keySet()) {
            double variance = 0.0;
            for (Map<Integer, Double> map : mapListPercentage) {
                double difference = (double) map.getOrDefault(interval, 0.0) - totalWaitPercentageMap.get(interval);
                variance += difference * difference;
            }
            variance = variance / (double) trials;
            stdDevMapPercentage.put(interval, Math.sqrt(variance));
        }

        props.setProperty("TOTAL_" + graphType + "_waitingTimeStdDev", String.valueOf(stdDevMap));
        props.setProperty("TOTAL_" + graphType + "_waitingTimeStdDevPercentage", String.valueOf(stdDevMapPercentage));
    }

    /**
     * Adds up the results for a given property from all the trials and returns the total
     *
     * @param props
     * @param graphType
     * @param property
     * @return
     */
    private static double getTotalOfProp(Properties props, String graphType, String property) {
        double total = 0.0;
        for (int i = 0; i < Integer.valueOf(props.getProperty("trials")); i++) {
            String key = graphType + "_" + i + "_" + property;
            total += Double.valueOf(props.getProperty(key));
        }
        return total;
    }

    /**
     *
     * @param i
     * @param graphList
     * @return
     */
    private static Graph getCorrectGraph(int i, List<Graph> graphList) {
        if (graphList.size() != 5) try {
            throw new Exception("graphlist for experiment has to have exactly 5 graphs");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (i < 10) return graphList.get(0);
        else if (i < 20) return graphList.get(1);
        else if (i < 30) return graphList.get(2);
        else if (i < 40) return graphList.get(3);
        else return graphList.get(4);
    }

    /**
     *
     * @param graph
     * @param timesteps
     * @param visual
     */
    private static void runPredefinedExperiment(Graph graph, int timesteps, boolean visual) {
        //generating controlled vehicles and requests
        List<Vehicle> vehicles = new ArrayList<>();
        vehicles.add(new Vehicle("v1", graph.getNode("A")));
        vehicles.add(new Vehicle("v2", graph.getNode("F")));

        List<Request> requests = new ArrayList<Request>();
        requests.add(new Request(1, graph.getNode("F"), graph.getNode("B"),0));
        requests.add(new Request(2, graph.getNode("A"), graph.getNode("E"),0));
        requests.add(new Request(3, graph.getNode("F"), graph.getNode("D"),0));
        requests.add(new Request(4, graph.getNode("A"), graph.getNode("B"),0));
        requests.add(new Request(5, graph.getNode("C"), graph.getNode("D"),0));
        requests.add(new Request(6, graph.getNode("D"), graph.getNode("C"),0));
        Map<Integer, List<Request>> requestMap = new HashMap<>();
        requestMap.put(0, requests);

        AmodSimulator simulator = new AmodSimulator(graph, visual, vehicles, requestMap, AssignmentType.ObjectSCRAM);

        if (visual) sleep(2500); //Makes the simulation start after the graph is drawn.

        for (int i = 0; i < timesteps; i++) {
            simulator.tick(graph, i);
            if (visual) sleep(50);
        }
    }

    /**
     *
     * @param folderPath
     * @return
     */
    private static List<Graph> getGraphsFromFolder(String folderPath) {
        List<Graph> graphList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> graphList.add(parseGraph("G" + String.valueOf(graphList.size()+1), path.toString())));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return graphList;
    }

    /**
     *
     * @param folderPath
     * @return
     */
    public static String[] getGraphTypes(String folderPath) {
        File file = new File(folderPath);
        return file.list((current, name) -> new File(current, name).isDirectory());
    }

    /**
     *
     * @param folderPath
     * @return
     */
    public static List<Properties> getPropertiesFromFolder(String folderPath) {
        List<Properties> propertiesList = new ArrayList<>();
        try (Stream<Path> paths = Files.walk(Paths.get(folderPath))) {
            paths
                    .filter(Files::isRegularFile)
                    .forEach(path -> propertiesList.add(Utility.loadProps(String.valueOf(path))));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return propertiesList;
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
     * Makes thread sleep
     */
    protected static void sleep(int duration) {
        try { Thread.sleep(duration); } catch (Exception e) {}
    }


}
