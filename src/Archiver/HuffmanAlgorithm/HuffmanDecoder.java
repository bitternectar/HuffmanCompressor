package Archiver.HuffmanAlgorithm;

import Archiver.IDecoder;

import java.util.ArrayList;
import java.util.List;
import java.util.PriorityQueue;

/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public class HuffmanDecoder implements IDecoder {
    private String fileExtension;

    /**
     *  Декомпрессия
     * @param compressedData - сжатые данные
     * @return Декомпрессированные данные
     */
    public byte[] decompress(byte[] compressedData) {
        HuffmanHeader hh = HuffmanHeader.getHeader(compressedData);

        fileExtension = hh.fName;

        PriorityQueue<Node> priorityQueue = ArchiveUtils.createPriorityQueue(hh.freqs);

        Node root = ArchiveUtils.createHuffmanTree(priorityQueue);

        byte[] decompressedData = decompressData(compressedData, hh.startIndex, hh.dataLength, root);

        return decompressedData;
    }

    @Override
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     *  Декодируем сжатые данные
     * @param compressedData - сжатые данные
     * @param startIndex - стартовый индекс
     * @param dataLength
     * @param root
     * @return byte[] - данные после декомпрессии
     */
    private byte[] decompressData(byte[] compressedData, int startIndex, long dataLength, Node root)
    {
        Node       current = root;
        List<Byte> data    = new ArrayList<>();

        for(int i = startIndex; i < startIndex + dataLength - 1; i++)
            for(int bit = 1; bit <= 128; bit <<= 1)
            {
                boolean zeroFlag = (compressedData[i] & bit) == 0;

                if(zeroFlag)
                    current = current.getLeftChild();
                else
                    current = current.getRightChild();

                if( current.isLeaf() )
                {
                    data.add ( current.getByte() );

                    current = root;
                }
            }

        byte[] result = new byte[data.size()];
        for(int i = 0; i < data.size(); i++) {
            result[i] = data.get(i).byteValue();
        }
        return result;
    }
}
