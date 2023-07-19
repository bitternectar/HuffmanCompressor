package Archiver.HuffmanAlgorithm;
/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public class Node implements Comparable<Node> {
    private byte b;
    private int frequency;
    private Node leftChild;
    private Node rightChild;

    public Node(byte b, int frequency) {
        this.b = b;
        this.frequency = frequency;
    }

    public Node(Node leftChild, Node rightChild) {
        this.frequency = leftChild.frequency + rightChild.frequency;
        this.leftChild = leftChild;
        this.rightChild = rightChild;
    }

    public byte getByte() {
        return b;
    }

    public int getFrequency() {
        return frequency;
    }

    public Node getLeftChild() {
        return leftChild;
    }

    public Node getRightChild() {
        return rightChild;
    }

    public boolean isLeaf() {
        return leftChild == null && rightChild == null;
    }

    @Override
    public int compareTo(Node other) {
        return frequency - other.frequency;
    }
}