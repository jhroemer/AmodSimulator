package AmodSimulator;

import java.util.Properties;

public class ResultParser {
    public static void main(String[] args) {
        Properties props = Utility.loadProps("data/experimentResults/test");
        System.out.println(props.getProperty("1-wait"));
    }
}
