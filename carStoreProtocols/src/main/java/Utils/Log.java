package Utils;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ConcurrentLinkedDeque;

public class Log {
    private final String path;
    private final String name;
    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final ConcurrentLinkedDeque<String> logStack = new ConcurrentLinkedDeque<>();

    public Log(String path, String name) {
        this.path = path;
        this.name = name;
        initializeLogFile();
        loadExistingLogEntries();
    }

    // inicializa o arquivo de log
    private void initializeLogFile() {
        File logFile = new File(path, name + ".txt");
        if (!logFile.exists()) {
            try {
                logFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void loadExistingLogEntries() {
        File logFile = new File(path, name + ".txt");
        if (logFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(logFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    logStack.addFirst(line); // carregando log existente
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public void write(String tag, String description) {
        String currentTime = LocalTime.now().format(timeFormatter);
        String logEntry = String.format("[%s] - %s - %s", tag, currentTime, description);
        logStack.push(logEntry); // nova entrada no topo da pilha

        Runnable task = () -> {
            synchronized (logStack) { // sincroniza o acesso a pilha
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(new File(path, name + ".txt"), true))) {
                    while (!logStack.isEmpty()) {
                        writer.write(logStack.pollLast()); // escreve do final da pilha para o início
                        writer.newLine();
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        new Thread(task).start();
    }
}
