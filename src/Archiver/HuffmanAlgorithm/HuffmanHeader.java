package Archiver.HuffmanAlgorithm;
/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
import java.nio.ByteBuffer;

/**
 * Служебная информация для последующей декомпрессии
 */
public class HuffmanHeader {
    public int startIndex;
    public String fName;
    public int[] freqs;
    public long   dataLength;
    public HuffmanHeader(){}
    public HuffmanHeader(int[] frequencyMap, String fileName, long dataLength, int startIndex) {
        freqs = frequencyMap;
        fName = fileName;
        this.dataLength = dataLength;
        this.startIndex = startIndex;
    }

    /**
     * Служебные данные для последующей декомпрессии
     * @param frequencyMap - таблица частот
     * @param fileName - расширение файла
     * @return byte[]
     */
    public static byte[] createHeader(int[] frequencyMap, String fileName, long dataLength){
        // Количество элементов в массиве frequencyMap
        int frequencyMapSize = frequencyMap.length;

        // Конвертация таблицы частот символов в байты
        ByteBuffer frequencyBuffer = ByteBuffer.allocate(frequencyMapSize * Integer.BYTES);
        for (int frequency : frequencyMap) {
            frequencyBuffer.putInt(frequency);
        }
        byte[] frequencyBytes = frequencyBuffer.array();

        // Количество байтов в имени файла
        byte[] fileNameBytes = fileName.getBytes();
        int fileNameLength = fileNameBytes.length;

        // Конвертация количества байтов сжатых данных в байты
        byte[] dataLengthBytes = ByteBuffer.allocate(Long.BYTES).putLong(dataLength).array();

        // Общая длина заголовка
        int headerLength = Integer.BYTES + frequencyBytes.length + Integer.BYTES + fileNameLength + Long.BYTES;

        // Создание заголовка
        ByteBuffer headerBuffer = ByteBuffer.allocate (headerLength);

        headerBuffer.putInt (frequencyMapSize);
        headerBuffer.put (frequencyBytes);
        headerBuffer.putInt (fileNameLength);
        headerBuffer.put (fileNameBytes);
        headerBuffer.put (dataLengthBytes);

        return headerBuffer.array();
    }

    /**
     *  Извлекаем служебную информацию
     * @param data - данные из архива
     * @return - HuffmanHeader -> служебная информация
     */
    public static HuffmanHeader getHeader(byte[] data)
    {
        ByteBuffer headerBuffer = ByteBuffer.wrap(data);

        // Чтение количества элементов в массиве frequencyMap
        int frequencyMapSize = headerBuffer.getInt();

        // Чтение элементов массива frequencyMap
        int[] frequencyMap = new int[frequencyMapSize];
        for (int i = 0; i < frequencyMapSize; i++) {
            frequencyMap[i] = headerBuffer.getInt();
        }

        // Чтение количества байтов в имени файла
        int fileNameLength = headerBuffer.getInt();

        // Чтение имени файла
        byte[] fileNameBytes = new byte[fileNameLength];
        headerBuffer.get(fileNameBytes);
        String fileName = new String(fileNameBytes);

        // Чтение количества байтов сжатых данных
        long dataLength = headerBuffer.getLong();

        // Определение индекса, начиная с которого заканчивается служебная информация
        int startIndex = headerBuffer.position();

        return new HuffmanHeader(frequencyMap, fileName, dataLength, startIndex);
    }
}
