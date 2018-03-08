package SCRAM;

public interface Node {
    //boolean visited = false;
    //Node previous = null;

    int getDistance(Node node);

    void setVisited(boolean visited);

    boolean isVisited();

    Node getPrevious();

    void setPrevious(Node previous);

    String getInfo();

    Node getType();
}