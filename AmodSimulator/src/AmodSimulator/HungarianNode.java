package AmodSimulator;

public interface HungarianNode {
    boolean visited = false;
    HungarianNode previous = null;

    String getInfo();
}