package AmodSimulator;

import java.util.Map;
import java.util.Properties;
import java.util.TreeMap;

public class AvgWaitParser {
    public static void main(String[] args) {
        Properties props = Utility.loadProps("data/experimentResults/chapter2.properties");

        Map<Integer, Integer> map = new TreeMap<>();
        for (int i = 0; i != 50; i += 5) map.put(i, 0);
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

                putIntoMap(j, map);
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
}
