import Archiver.Archiver;
import Archiver.HuffmanAlgorithm.HuffmanArchiver;

import javax.swing.*;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.awt.event.*;
/**
 * @author Smirnovskiy Mikhail
 * @version 1.0
 * */
public class MainForm extends JFrame {
    // Поле ввода пути к файлам
    private JTextField inputField;
    // Текстовая область для вывода логов
    private JTextArea logArea;
    // Прогресс-бар
    private JProgressBar progressBar;
    // Кнопка "Сжать"
    private JButton compressButton;
    // Кнопка "Извлечь"
    private JButton decompressButton;
    // Кнопка "Обзор"
    private JButton browseButton;
    private JPanel mainPanel;
    private JLabel progressLabel;
    //Поле для ввода имени архива
    private JTextField outputField;

    private Integer bytes;

    public MainForm() {
        setTitle("Huffman Archiver");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(600, 800);
        initComponents();
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    private void initComponents() {
        outputField.setText("Compressed");
        progressLabel.setText("");
        compressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String inputPath = inputField.getText();
                String outputPath = outputField.getText();

                if (inputPath.isEmpty()) {
                    logMessage("Пожалуйста, выберите файлы");
                    return;
                }

                if (outputPath.isEmpty()) {
                    logMessage("Пожалуйста, введите имя архива");
                    return;
                }

                logMessage("Запуск архивации ...");

                toggleBtnsEnable(false);

                try
                {
                    compress(inputPath, outputPath);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });

        decompressButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e)
            {
                String inputPath  = inputField.getText();
                String outputPath = "decompressed";

                logMessage("Запуск декомпрессии ...");

                toggleBtnsEnable(false);
                try
                {
                    decompress(inputPath, outputPath);
                }
                catch (IOException ex)
                {
                    throw new RuntimeException(ex);
                }
            }
        });

        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser fileChooser = new JFileChooser();
                fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
                fileChooser.setMultiSelectionEnabled(true);

                int result = fileChooser.showOpenDialog(MainForm.this);

                if (result == JFileChooser.APPROVE_OPTION)
                {
                    File[]        selectedFiles = fileChooser.getSelectedFiles();
                    StringBuilder sb            = new StringBuilder();
                    for (File file : selectedFiles)
                    {
                        sb.append(file.getAbsolutePath()).append(";");
                    }
                    inputField.setText(sb.toString());
                }
            }
        });

        add(mainPanel);
    }

    private void compress(String inputPath, String outputPath) throws IOException {
        ArrayList<File> files = collectFiles(inputPath);

        Archiver archiver = new HuffmanArchiver();

        archiver.setOutputFileName(outputPath);

        initArchiver(archiver);

        new Thread(()->{ archiver.archiveFiles(files);}).start();
    }


    private void decompress(String inputPath, String outputPath) throws IOException {
        ArrayList<File> files = collectFiles(inputPath);
        Archiver archiver = new HuffmanArchiver();

        archiver.setOutputFileName(outputPath);

        initArchiver(archiver);

        new Thread(()->{ archiver.extractFiles(files.get(0));}).start();
    }

    /**
     *  Создаем список файлов
     * @param inputPath
     * @return
     */
    private ArrayList<File> collectFiles(String inputPath) {
        ArrayList<File> files = new ArrayList<>();
        String[] paths = inputPath.split(";");

        for(String path : paths)
        {
            if(!path.isEmpty())
                files.add(new File(path));
        }

        return files;
    }

    /**
     * Логирование
     * @param message
     */
    private void logMessage(String message) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                SimpleDateFormat dateFormat = new SimpleDateFormat("dd.MM.yy HH:mm:ss");
                String formattedDate = dateFormat.format(new Date());

                logArea.append("[" + formattedDate + "]   " + message + "\n");
                logArea.setCaretPosition(logArea.getDocument().getLength());
            }
        });
    }

    private void toggleBtnsEnable(boolean b) {
        SwingUtilities.invokeLater(()-> {
            compressButton.setEnabled(b);
            decompressButton.setEnabled(b);
        });
    }

    private void initArchiver(Archiver archiver)
    {
        archiver.setLogCallback(this::logMessage);

        archiver.setLogBytesCountCallback((bytesCount)->{
            SwingUtilities.invokeLater(()->{
                bytes = bytesCount;
                progressBar.setMaximum(bytesCount);
                progressLabel.setText(String.format("0/%d", bytes));
            });
        });

        archiver.setProgressCallback((progress)->{
            SwingUtilities.invokeLater(()->{
                progressBar.setValue(progressBar.getValue() + progress);

                progressLabel.setText(String.format("%d/%d",progressBar.getValue(), bytes));
            });
        });

        archiver.setOnResultListener( () ->{
            logMessage("Архив успешно создан");
            progressBar.setValue(0);
            progressLabel.setText("");
            toggleBtnsEnable(true);
        });
    }
}