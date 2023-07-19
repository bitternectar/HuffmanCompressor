package Archiver.HuffmanAlgorithm;

import java.util.PriorityQueue;
/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public class ArchiveUtils {
    /**
     * Создание таблицы частот
     * @param data - массив байтов
     * @return int[]
     */
    public static int[] createFrequencyMap(byte[] data) {
        int[] frequencyMap = new int[256];

        for (byte b : data) {
            frequencyMap[b & 0xFF] += 1;
        }

        return frequencyMap;
    }

    /**
     * Создание приоритетной очередь на основе таблицы частот
     * @param frequencyMap - таблица частот
     * @return PriorityQueue<Node>
     */
    public static PriorityQueue<Node> createPriorityQueue(int[] frequencyMap){
        PriorityQueue<Node> priorityQueue = new PriorityQueue<>();

        for (int i = 0; i < 256; i++) {
            int  frequency = frequencyMap[i];
            if(frequency > 0)
            {
                byte b = (byte) i;

                priorityQueue.offer(new Node((byte) (b & 0xFF), frequency));
            }
        }

        return priorityQueue;
    }

    /**
     * Составляем дерево Хаффмана
     * @param priorityQueue
     * @return root Node
     */
    public static Node createHuffmanTree(PriorityQueue<Node> priorityQueue) {

        while (priorityQueue.size() > 1) {
            Node leftChild  = priorityQueue.poll();
            Node rightChild = priorityQueue.poll();
            Node parent     = new Node(leftChild, rightChild);

            priorityQueue.offer(parent);
        }

        return priorityQueue.poll();
    }

    /**
     * Обход дерева в глубину и построение кодовой таблицы
     * @param node - root узел дерева
     */
    public static String[] buildCodeTable(Node node) {
        String[] codes = new String[256];
        Next(node, "", codes);
        return codes;
    }
    private static void Next(Node node, String code, String[] codes)
    {
        if(node.isLeaf()){
            codes[node.getByte() & 0xFF] = code;
        }
        else{
            Next(node.getLeftChild(), code + '0', codes);
            Next(node.getRightChild(), code + '1', codes);
        }
    }
}
