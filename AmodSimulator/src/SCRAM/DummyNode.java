package SCRAM;

public class DummyNode implements Node {


    @Override
    public int getDistance(Node node) {
        return 0;
    }

    @Override
    public String getInfo() {
        return "DummyNode";
    }
}
