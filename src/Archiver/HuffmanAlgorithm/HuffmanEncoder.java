package Archiver.HuffmanAlgorithm;
import Archiver.ArchiveExtensions;
import Archiver.IEncoder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.*;

/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public class HuffmanEncoder implements IEncoder {
    int[] frequencyMap;
    String[] codes;

    private static final ArchiveExtensions ext = ArchiveExtensions.HUFF;
    /**
     * Реализация метода сжатия на основе кода Хаффмана
     * @param data - исходные данные
     * @return Сжатые данные
     */
    @Override
    public byte[] compress(byte[] data, String fileExtension) throws IOException {
        // Подсчет частоты появления каждого байта
        frequencyMap = ArchiveUtils.createFrequencyMap (data);

        // Создание приоритетной очереди для узлов дерева Хаффмана
        PriorityQueue<Node> priorityQueue = ArchiveUtils.createPriorityQueue (frequencyMap);

        //Строим дерево Хаффмана
        Node root = ArchiveUtils.createHuffmanTree (priorityQueue);

        //Таблица кодов Хаффмана
        codes = ArchiveUtils.buildCodeTable (root);

        byte[] compressedData = compressData (data, codes);

        //Служебная информация
        byte[] header = HuffmanHeader.createHeader (frequencyMap, fileExtension, compressedData.length);


        ByteArrayOutputStream outputStream = new ByteArrayOutputStream ();
        outputStream.write ( header, 0, header.length );
        outputStream.write ( compressedData, 0, compressedData.length );

        byte[] result = outputStream.toByteArray();

        outputStream.close();

        return result;
    }

    /**
     * Кодируем исходные данные
     * @param  data   - исходные данные
     * @param  codes  - Таблица кодов Хаффмана
     * @return byte[] - Сжатые данные
     */
    private byte[] compressData(byte[] data, String[] codes) {
        ArrayList<Byte> bytes = new ArrayList<>();
        byte sum = 0;
        int bit = 1;

        for(byte b : data)
        {
            for(char c : codes[b & 0xFF].toCharArray())
            {
                if(c == '1')
                    sum |= (byte)bit;
                if(bit < 128)
                    bit <<= 1;
                else{
                    bytes.add(sum);
                    sum = 0;
                    bit = 1;
                }
            }
        }
        if(bit > 1)
            bytes.add(sum);

        byte[] result = new byte[bytes.size()];
        for(int i = 0; i < bytes.size(); i++) {
            result[i] = bytes.get(i).byteValue();
        }

        return result;
    }

    @Override
    public ArchiveExtensions getEncoderExtension() {
        return ext;
    }
}
