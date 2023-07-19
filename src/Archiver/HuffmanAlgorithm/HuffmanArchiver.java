package Archiver.HuffmanAlgorithm;

import Archiver.Archiver;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.util.*;
import java.util.concurrent.*;

/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public class HuffmanArchiver extends Archiver {
    ExecutorService executorService;

    Long startExecuteTime;
    Long endExecuteTime;
    public HuffmanArchiver(){
        setEncoder(new HuffmanEncoder());
        setDencoder(new HuffmanDecoder());
    }

    /**
     *  Сжатие данных и запись в архив
     * @param files - список файлов
     */
    @Override
    public void archiveFiles(ArrayList<File> files)
    {
        executorService   = Executors.newCachedThreadPool();
        String parentPath = files.get(0).getParent ();

        List<byte[]>      resultList = Collections.synchronizedList (new ArrayList<byte[]>());

        startExecuteTime = System.currentTimeMillis();

        ArrayList<Future> futures    = startCompressExecution(files, resultList);
        // Получаем ссылку на ThreadPoolExecutor
        ThreadPoolExecutor threadPoolExecutor = (ThreadPoolExecutor) executorService;
        int threadCount = threadPoolExecutor.getActiveCount();
        try
        {
            //Дожидаемся завершения работы потоков
            for(Future future : futures){
                future.get();
            }
            endExecuteTime = System.currentTimeMillis();

            writeCompressedDataToFile(resultList, parentPath);

            Long totalExecuteTime   = endExecuteTime - startExecuteTime;
            // Вычисляем среднее время выполнения одного потока
            Long averageExecuteTime = totalExecuteTime / threadCount;

            log.accept(String.format(
                    "\n--Количество использованых потоков:%d\n--Общее время работы потоков: %d мс\n--Среднее время выполнения: %d мс",
                    threadCount, totalExecuteTime, averageExecuteTime
            ));

            log.accept("Запускаем задачу в одном потоке...");

            startExecuteTime = System.currentTimeMillis();

            singleThreadCompress(files);

            Long totalSingleExecuteTime   = endExecuteTime - startExecuteTime;

            log.accept(String.format(
                    "\n--Общее время работы в одном потоке: %d мс\n--Ускорение: %f ",
                    totalSingleExecuteTime, ((double)totalSingleExecuteTime/totalExecuteTime)
            ));

            onResultListener.execute();

        }
        catch (Throwable e)
        {
            e.printStackTrace();
        }
    }
    /**
     *  Извлекаем файлы из архива
     * @param file - файл архива
     */
    @Override
    public void extractFiles(File file) {
        try
        {
            executorService = Executors.newCachedThreadPool ();

            String parentPath = file.getParent ();
            byte[] data       = Files.readAllBytes (file.toPath());

            ByteBuffer bb = ByteBuffer.wrap(data);

            int filesCount = bb.getInt();

            byte [] compressedData = Arrays.copyOfRange(data, bb.position(), data.length);

            List<HuffmanHeader> headers = getHeaders( filesCount, compressedData);

            fCount.accept(headers.size());

            //Парсим сжатые данные и добавляем массивы в потокобезопасный List
            List<byte[]>        compressedDataList = createCompressDataSyncList (headers, compressedData);
            //Потокобезопасная хэш-таблица для записи результатов декомпрессии
            Map<String, byte[]> resultList         = Collections.synchronizedMap (new HashMap<String, byte[]>());
            //Запускаем процесс декомпрессии
            ArrayList<Future>   futures            = startDecompressExecute (compressedDataList, resultList, headers);

            //Дожидаемся окончания работы потоков
            for(Future f : futures)
            {
                f.get();
            }

            futures.clear();
            //Записываем исходные данные в файлы
            resultList.forEach((name, arr)->{
                futures.add(executorService.submit(() -> {
                    File decompressedFile = new File (parentPath + "\\" + name);

                    try
                    {
                        if ( decompressedFile.createNewFile () )
                        {
                            log.accept(String.format("%s: %s", name, "Запись в файл ..."));
                            FileOutputStream outputStream = new FileOutputStream (decompressedFile);
                            outputStream.write (arr);
                            outputStream.close ();
                            log.accept(String.format("%s: %s", name, "Запись в файл прошла успешно"));
                        }
                    }
                    catch (IOException e)
                    {
                        log.accept (e.getMessage());
                    }

                }));
            });
            for(Future f : futures)
            {
                f.get();
            }
            onResultListener.execute();
        }
        catch (IOException | ExecutionException | InterruptedException e)
        {
            log.accept (e.getMessage());
        }
    }

    /**
     *  Запускаем процесс сжатия данных
     * @param files - список файлов, которые необходимо сжать и добавить в архив
     * @param resultList - потокобезопасный список для хранения сжатых данных
     * @return
     */
    private ArrayList<Future> startCompressExecution(ArrayList<File> files, List<byte[]> resultList)
    {
        ArrayList<Future> futures = new ArrayList<>();

        for (File file : files)
        {
            fCount.accept(files.size());

            futures.add (executorService.submit (()->{
                if( file.exists () && file.isFile () )
                {
                    String fileName =  file.getName();
                    try
                    {
                        log.accept ( String.format("%s: %s", fileName, "сжатие...") );
                        byte[] data           = Files.readAllBytes (file.toPath());

                        byte[] compressResult = new HuffmanEncoder().compress (data, fileName);

                        progress.accept(1);
                        log.accept ( String.format("%s: %s", fileName, "сжатие прошло успешно") );
                        resultList.add(compressResult);
                    }
                    catch (IOException e)
                    {
                        throw new RuntimeException(e);
                    }
                }
            }));
        }

        return futures;
    }

    /**
     *  Запись сжатых данных в архив
     * @param resultList - сжатые данные
     * @param parentPath - директория, в которой будет создан файл архива
     */
    private void writeCompressedDataToFile(List<byte[]> resultList, String parentPath)
    {
        File file = new File ( String.format("%s\\%s.%s", parentPath, outputFileName, encoder.getEncoderExtension()));

        try
        {
            if (file.createNewFile())
            {
                FileOutputStream outputStream = new FileOutputStream(file);
                DataOutputStream dos          = new DataOutputStream(outputStream);

                dos.writeInt(resultList.size());

                int index = 0;

                for(byte[] data : resultList)
                {
                    log.accept ( String.format("%s: %s - %d / %d ...", file.getName(), "запись данных в архив", ++index, resultList.size()) );
                    dos.write (data, 0, data.length);
                }

                dos.close ();
                outputStream.close ();

                executorService.shutdown ();
            }
        }
        catch (IOException e)
        {
            log.accept (e.getMessage());
        }
    }


    /**
     *  Извлекаем служебную информацию
     * @param filesCount - количество сжатых файлов
     * @param bytes - сжатые файлы
     * @return список HuffmanHeader
     */
    private List<HuffmanHeader> getHeaders(int filesCount, byte[] bytes)
    {
        int start = 0;
        int end   = bytes.length;

        List<HuffmanHeader> headers = new ArrayList<>();

        for(int i = 0; i < filesCount; i++)
        {
            byte[] arr = Arrays.copyOfRange(bytes, start, end);
            HuffmanHeader hh = HuffmanHeader.getHeader(arr);

            start += hh.startIndex + hh.dataLength ;

            headers.add(hh);
        }

        return headers;
    }

    /**
     *  Извлекаем массивы с сжатыми данными для каждого файла
     * @param headers - список со служебной информацией
     * @param compressedData - сжатые данные
     * @return потокобезопасный список
     */
    private List<byte[]> createCompressDataSyncList(List<HuffmanHeader> headers, byte[] compressedData)
    {
        List<byte[]> compressedDataList = Collections.synchronizedList (new ArrayList<byte[]>());

        int startIndex = 0;
        int endIndex   = 0;

        for (HuffmanHeader header : headers)
        {
            startIndex = endIndex;
            endIndex   += header.startIndex + header.dataLength;

            byte [] arr = Arrays.copyOfRange(compressedData, startIndex, compressedData.length);
            compressedDataList.add(arr);
        }

        return compressedDataList;
    }

    /**
     *  Декомпрессия данных в отдельных потоках
     * @param compressedDataList - потокобезопасный список с массивами сжатых данных
     * @param resultList - потокобезопасный список для записи извлеченных данных
     * @param headers - список с служебной информацией
     * @return
     */
    private ArrayList<Future> startDecompressExecute(
            List<byte[]> compressedDataList,
            Map<String,byte[]> resultList,
            List<HuffmanHeader> headers
    ) {

        ArrayList<Future> futures = new ArrayList<>();

        int index = 0;
        for(byte[] arr : compressedDataList)
        {
            String fileName = headers.get(index++).fName;

            futures.add( executorService.submit (() -> {
                log.accept(String.format("%s: %s", fileName, "Декомпрессия ..."));
                resultList.put(fileName, new HuffmanDecoder().decompress(arr) );
                log.accept(String.format("%s: %s", fileName, "Декомпрессия прошла успешно."));
                progress.accept(1);
            }));
        }

        return futures;
    }

    private void singleThreadCompress(List<File> files)
    {

        for (File file : files)
        {
            if( file.exists () && file.isFile () )
            {
                String fileName =  file.getName();
                try
                {
                    byte[] data           = Files.readAllBytes (file.toPath());

                    byte[] compressResult = new HuffmanEncoder().compress (data, fileName);
                }
                catch (IOException e)
                {
                    throw new RuntimeException(e);
                }
            }
        }
        endExecuteTime = System.currentTimeMillis();
    }
}
