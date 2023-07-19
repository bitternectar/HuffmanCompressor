package Archiver;
/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public interface IDecoder {
    byte[] decompress(byte[] data);
    String getFileExtension();
}
