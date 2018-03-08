package AmodSimulator;

import java.util.Properties;

public class ResultParser {
    public static void main(String[] args) {
        Properties props = Utility.loadProps("data/experimentResults/scram1exp.properties");

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

        for (int i = 0; i < Integer.valueOf(props.getProperty("trials")); i++) {
            String k = "medium_" + i + "_unoccupied";
            System.out.println(k);
            System.out.println(props.getProperty(k));
            System.out.println("\n");
        }
//
//        props.getProperty("TOTAL_G1_avgUnoccupied");
//        props.getProperty("TOTAL_G1_avgWait");
//        props.getProperty("TOTAL_G2_avgUnoccupied");
//        props.getProperty("TOTAL_G2_avgWait");
    }
}
