package SCRAM;

public interface Node {
    boolean visited = false;
    Node previous = null;

    int getDistance(Node node);

    String getInfo();
}