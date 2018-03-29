package AmodSimulator;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class AvgWaitParser {
    public static void main(String[] args) {
        Properties props = Utility.loadProps("data/experimentResults/chapter2.properties");
        double interval = 0.1;

        Map<Double, Integer> map;
        String[] graphTypes = ExperimentRunner.getGraphTypes(props.getProperty("graphDir")); // todo: should getGraphTypes be in Utility?

        for (String s : graphTypes) {
            map = new TreeMap<>();
            for (int n = 0; n <= 200; n++) { // adding 0 for each interval
                double i = interval * n;
                int scale = (int) Math.pow(100, 1);
                double currentBin = (double) Math.round(i * scale) / scale;
                map.put(currentBin, 0);
            }

            // go through trials, add the avgWait value to the correct bin in map
            for (int i = 0; i < Integer.valueOf(props.getProperty("trials")); i++) {
//                System.out.println("i is: " + i);
                String k = s + "_" + i + "_avgWait";

//                System.out.println(k);
//                System.out.println(props.getProperty(k));
//                System.out.println("\n");

                double avgWait = Double.parseDouble(props.getProperty(k));
                double avgWaitMinutes = avgWait * 5;
                System.out.println("AVG WAIT MINUTES, " + s + ": " + avgWaitMinutes);

                putIntoBin(avgWaitMinutes, map, interval);
            }

            StringBuilder coordinates = new StringBuilder();
            boolean notZero = false;
            for (Double i : map.keySet()) {
                if (map.get(i) != 0) notZero = true;
                if (notZero) coordinates.append("(" + i + "," + map.get(i) + ")");
            }
            System.out.println("Coordinates for : " + s);
            System.out.println(coordinates);
        }
    }

    /**
     *
     * @param avgWait
     * @param map
     */
    private static void putIntoBin(double avgWait, Map<Double, Integer> map, double interval) {
        double latestBin = 0.0;

        for (int n = 0; n <= 200; n++) { // finding out which interval value corresponds to
            double i = interval * n; // floating point
            int scale = (int) Math.pow(100, 1);
            double currentBin = (double) Math.round(i * scale) / scale;
            if (avgWait < currentBin) {
                map.put(latestBin, 1+map.get(latestBin));
                break;
            }
            latestBin = currentBin;
        }
    }
}
