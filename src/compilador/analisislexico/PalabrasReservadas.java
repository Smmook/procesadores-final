package compilador.analisislexico;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class PalabrasReservadas {
    private final HashMap<String, String> keywords;

    public PalabrasReservadas(String fileName) {
        keywords = new HashMap<>();
        readFromFile(fileName);
    }

    public String getTipo(String lexema) {
        return this.keywords.get(lexema);
    }

    public String getLexema(String tipo) {
        for (Map.Entry<String, String> entry : keywords.entrySet()) {
            if (entry.getValue().equals(tipo)) {
                return entry.getKey();
            }
        }
        return null;
    }

    private void readFromFile(String fileName) {
        try {
            BufferedReader reader = new BufferedReader(new FileReader(fileName));
            while (true) {
                String line = reader.readLine();
                if (line == null) break;
                procesarLinea(line);
            }
            reader.close();
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }
    }

    private void procesarLinea(String line) {
        String trimmed = line.trim();
        if (trimmed.isEmpty()) return;
        if (trimmed.charAt(0) == '/' && trimmed.charAt(1) == '/') return;
        String[] entry = trimmed.split("\\s+");
        keywords.put(entry[1], entry[0]);
    }
}