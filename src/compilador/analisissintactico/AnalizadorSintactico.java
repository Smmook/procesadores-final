package compilador.analisissintactico;

import compilador.analisislexico.AnalizadorLexico;
import compilador.analisislexico.Token;

import java.util.Hashtable;
import java.util.Map;
import java.util.Set;

public class AnalizadorSintactico {
    private AnalizadorLexico lexico;
    private Token token;
    private Hashtable<String, String> simbolos;
    private String tipo;
    private int tamanoArray;

    public AnalizadorSintactico(AnalizadorLexico lexico) {
        this.lexico = lexico;
        this.token = this.lexico.getNextToken();
        this.simbolos = new Hashtable<>();
    }

    

    public String tablaSimbolos() {
        String simbolos = "";

        Set<Map.Entry<String, String>> s = this.simbolos.entrySet();
        if (s.isEmpty()) System.out.println("Tabla de simbolos vacia");
        for (Map.Entry<String, String> m : s) {
            simbolos = simbolos + "<'" + m.getKey() + "', " + m.getValue() + "> \n";
        }

        return simbolos;
    }

    private void compara(String token) {
        if (this.token.getEtiqueta().equals(token)) {
            this.token = this.lexico.getNextToken();
        } else {
            System.out.println("Error en linea " + this.lexico.getLineas() + ". Expected " + token + ", Found " + this.token.getEtiqueta());
        }
    }

}
