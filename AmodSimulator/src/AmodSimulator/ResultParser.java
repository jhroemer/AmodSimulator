package AmodSimulator;

import java.util.Properties;

public class ResultParser {
    public static void main(String[] args) {
        Properties props = Utility.loadProps("data/experimentResults/scram1exp");

        props.getProperty("TOTAL_G1_avgUnoccupied");
        props.getProperty("TOTAL_G1_avgWait");
        props.getProperty("TOTAL_G2_avgUnoccupied");
        props.getProperty("TOTAL_G2_avgWait");
    }
}
