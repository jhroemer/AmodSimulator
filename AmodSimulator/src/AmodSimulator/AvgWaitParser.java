package AmodSimulator;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class AvgWaitParser {
    public static void main(String[] args) {
        Properties props = Utility.loadProps("data/experimentResults/chapter2.properties");

        Map<Integer, Integer> map = new TreeMap<>();
        for (int i = 0; i < 51; i += 2) map.put(i, 0);
        System.out.println(map);
        /*
        StringBuilder s = new StringBuilder();

        s.append("\begin{tikzpicture}");
        s.append("\uFEFF\\begin{axis}[\n" +
                "    ybar,\n" +
                "    enlargelimits=0.15,\n" +
                "    legend style={at={(0.5,-0.15)},\n" +
                "      anchor=north,legend columns=-1},\n" +
                "    ylabel={Avg. unoccpupied km's driven},\n" +
                "    symbolic x coords={1,2,3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16},\n" +
                "    xtick=data,\n" +
                "    nodes near coords,\n" +
                "    nodes near coords align={vertical},\n" +
                "    ]");
        */

        String[] graphTypes = ExperimentRunner.getGraphTypes(props.getProperty("graphDir")); // todo: should getGraphTypes be in Utility?

        for (String s : graphTypes) {
            for (int i = 0; i < Integer.valueOf(props.getProperty("trials")); i++) {
                System.out.println("i is: " + i);
                String k = s + "_" + i + "_avgWait";
                System.out.println(k);
                System.out.println(props.getProperty(k));
                System.out.println("\n");

                double d = Double.parseDouble(props.getProperty(k));
                int j = (int) Math.round(d);

                System.out.println("j is: " + j);

                // putIntoMap(j, map);
                putIntoTwoBin(j, map);
            }
            System.out.println(map);

            StringBuilder coordinates = new StringBuilder();
            for (Integer i : map.keySet()) {
                coordinates.append("(" + i + "," + map.get(i) + ")");
            }
            System.out.println("Coordinates for : " + s);
            System.out.println(coordinates);
        }
    }

    // bin size = 5
    private static void putIntoMap(int j, Map<Integer, Integer> map) {
        if (j < 5) map.put(0, 1+map.get(0));
        else if (j < 10) map.put(5, 1+map.get(5));
        else if (j < 15) map.put(10, 1+map.get(10));
        else if (j < 20) map.put(15, 1+map.get(15));
        else if (j < 25) map.put(20, 1+map.get(20));
        else if (j < 30) map.put(25, 1+map.get(25));
        else if (j < 35) map.put(30, 1+map.get(30));
        else if (j < 40) map.put(35, 1+map.get(35));
        else if (j < 45) map.put(40, 1+map.get(40));
        else if (j < 50) map.put(45, 1+map.get(45));
    }

    // bin size = 2
    private static void putIntoTwoBin(int j, Map<Integer, Integer> map) {
        if (j < 2) map.put(0, 1+map.get(0));
        else if (j < 4) map.put(2, 1+map.get(2));
        else if (j < 6) map.put(4, 1+map.get(4));
        else if (j < 8) map.put(6, 1+map.get(6));
        else if (j < 10) map.put(8, 1+map.get(8));
        else if (j < 12) map.put(10, 1+map.get(10));
        else if (j < 14) map.put(12, 1+map.get(12));
        else if (j < 16) map.put(14, 1+map.get(14));
        else if (j < 18) map.put(16, 1+map.get(16));
        else if (j < 20) map.put(18, 1+map.get(18));
        else if (j < 22) map.put(20, 1+map.get(20));
        else if (j < 24) map.put(22, 1+map.get(22));
        else if (j < 26) map.put(24, 1+map.get(24));
        else if (j < 28) map.put(26, 1+map.get(26));
        else if (j < 30) map.put(28, 1+map.get(28));
        else if (j < 32) map.put(30, 1+map.get(30));
        else if (j < 34) map.put(32, 1+map.get(32));
        else if (j < 36) map.put(34, 1+map.get(34));
        else if (j < 38) map.put(36, 1+map.get(36));
        else if (j < 40) map.put(38, 1+map.get(38));
        else if (j < 42) map.put(40, 1+map.get(40));
        else if (j < 44) map.put(42, 1+map.get(42));
        else if (j < 46) map.put(44, 1+map.get(44));
        else if (j < 48) map.put(46, 1+map.get(46));
        else if (j < 50) map.put(48, 1+map.get(48));
        else if (j < 52) map.put(50, 1+map.get(50));
    }
}
