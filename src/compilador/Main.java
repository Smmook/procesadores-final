package compilador;

import compilador.analisislexico.AnalizadorLexico;
import compilador.analisissintactico.AnalizadorSintactico;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Main {
    public static void main(String[] args) {
        try {
            String source = Files.readString(Path.of("res/programa.txt"));

            AnalizadorSintactico analizadorSintactico = new AnalizadorSintactico(new AnalizadorLexico(source));
            analizadorSintactico.analisis();

            System.out.println("\nTabla de simbolos");
            System.out.println(analizadorSintactico.tablaSimbolos());
        } catch (IOException e) {
            System.err.println(e.getMessage());
        }
    }
}
