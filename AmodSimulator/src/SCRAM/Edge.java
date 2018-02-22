package SCRAM;

public class Edge implements Comparable<Edge> {
    int weight;
    int start;
    int end;

    public Edge(int start, int end, int weight) {
        this.weight = weight;
        this.start = start;
        this.end = end;
    }

    public int getStart() {
        return start;
    }

    public int getEnd() {
        return end;
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