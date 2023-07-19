package Archiver;

public enum ArchiveExtensions {
    HUFF(".huff");

    private String extension;

    ArchiveExtensions(String s) {
        extension = s;
    }

    public  String getName()
    {
        return extension;
    }
}
