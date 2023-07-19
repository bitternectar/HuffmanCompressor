package Archiver;

import java.io.IOException;

/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public interface IEncoder {
    byte[] compress(byte[] data, String fileExtension) throws IOException;

    ArchiveExtensions getEncoderExtension();
}
