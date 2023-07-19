package Archiver;

import java.io.File;
import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * Абстрактный класс архиватора.
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public abstract class Archiver {
    protected IEncoder encoder;
    protected IDecoder decoder;
    protected String outputFileName;

    protected Consumer<String> log;
    protected Consumer<Integer> progress;
    protected Consumer<Integer> fCount;
    protected Callback onResultListener;
    /**
     * Архивация файлов и директорий
     * @param files - список файлов и директорий
     */
    public abstract void archiveFiles(ArrayList<File> files);

    /**
     * Извлечение файлов из архива
     * @param file - файл архива
     */
    public abstract void extractFiles(File file);
    public void setEncoder(IEncoder encoder){
        this.encoder = encoder;
    }
    public void setDencoder(IDecoder decoder){
        this.decoder = decoder;
    }
    public IEncoder getEncoder(){
        return encoder;
    }
    public IDecoder getDecoder(){
        return decoder;
    }

    public void setLogCallback(Consumer<String> callback){log = callback;}
    public void setProgressCallback(Consumer<Integer> callback){progress = callback;}
    public void setLogBytesCountCallback(Consumer<Integer> callback){
        fCount = callback;}
    public void setOnResultListener(Callback callback){
        onResultListener = callback;}
    /**
     * Устанавливаем имя для архива
     * @param outputFileName - Имя выходного файла
     */
    public void setOutputFileName(String outputFileName){
        this.outputFileName = outputFileName;
    }
}
