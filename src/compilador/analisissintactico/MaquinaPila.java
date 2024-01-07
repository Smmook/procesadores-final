package compilador.analisissintactico;

import java.util.Stack;

public class MaquinaPila {
    private final Stack<String> pila;

    public MaquinaPila() {
        this.pila = new Stack<>();
    }

    public void push(String valor) {
        pila.push("push " + valor);
    }

    public void lvalue(String id) {
        pila.push("lvalue " + id);
    }

    public void operator(String op) {
        pila.push(op);
    }

    public void rvalue(String id) {
        pila.push("rvalue " + id);
    }

    public void halt() {
        pila.push("halt");
    }

    public void goto_(int label) {
        pila.push("goto label_" + label);
    }

    public void gofalse(int label) {
        pila.push("gofalse label_" + label);
    }

    public void gotrue(int label) {
        pila.push("gotrue " + label);
    }

    public void label(int label) {
        pila.push("label_" + label +":");
    }

    public void print(String id) {
        pila.push("print " + id);
    }

    public int lastLabel() {
        int label = -1;

        for (String instruccion : pila) {
            if (instruccion.contains("label_")) {
                int pos = instruccion.lastIndexOf("_");
                int numero = Integer.parseInt(instruccion.substring(pos + 1, instruccion.length()).split(":")[0]);
                if (numero > label) {
                    label = numero;
                }
            }
        }

        return label;
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String s : pila) {
            sb.append(s).append("\n");
        }
        return sb.toString();
    }

}
