package SCRAM;

public class Edge implements Comparable<Edge> {
    int weight;
    Node startNode;
    Node endNode;
    // for MacAlpine SCRAM implementation
    int startIndex;
    int endIndex;

    public Edge(int start, int end, int weight) {
        this.weight = weight;
        this.startIndex = start;
        this.endIndex = end;
    }

    public int getStartIndex() {
        return startIndex;
    }

    public int getEndIndex() {
        return endIndex;
    }

    @Override
    public int compareTo(Edge other) {
        //noinspection Duplicates
        if (this.weight < other.weight) return -1;
        else if (this.weight == other.weight) return 0;
        else return 1;
    }

    public int getWeight() {
        return weight;
    }

    public void setWeight(int weight) {
        this.weight = weight;
    }
}