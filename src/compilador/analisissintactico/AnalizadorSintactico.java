package compilador.analisissintactico;

import compilador.analisislexico.AnalizadorLexico;
import compilador.analisislexico.Token;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Hashtable;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

public class AnalizadorSintactico {
    private AnalizadorLexico lexico;
    private Token token;
    private Hashtable<String, String> simbolos;
    private String tipo;
    private int tamanoArray;
    private boolean errores;
    private final MaquinaPila pila;
    private boolean soloPrimitivos;

    public AnalizadorSintactico(AnalizadorLexico lexico) {
        this.lexico = lexico;
        this.token = this.lexico.getNextToken();
        this.simbolos = new Hashtable<>();
        this.errores = false;
        this.pila = new MaquinaPila();
        this.soloPrimitivos = true;
    }

    public void analisis() {
        programa();
        if (!errores) {
            System.out.println("Programa compilado correctamente");

            if (this.soloPrimitivos) {
                codigoIntermedio();
            } else {
                System.out.println("No se ha generado codigo intermedio ya que el programa contiene tipos no primitivos.");
            }
        }
    }

    private void programa() {
        compara("void");
        compara("main");
        compara("open_bracket");
        declaraciones();
        instrucciones();
        compara("closed_bracket");
        pila.halt();
    }

    private void declaraciones() {
        if (tipoCoincideCon("boolean", "float", "int")) {
            declaracionVariable();
            declaraciones();
        }
    }

    private void declaracionVariable() {
        tipoVariable();
        declaracionVariablePrima();
        compara("semicolon");
    }

    private void declaracionVariablePrima() {
        if (tipoCoincideCon("open_square_bracket")) {
            this.soloPrimitivos = false;    // Hay arrays
            compara("open_square_bracket");
            if (!this.token.getEtiqueta().equals("int")) {
                error("int");
                this.tamanoArray = 0;
            } else {
                this.tamanoArray = Integer.parseInt(this.token.getLexema());
            }
            this.token = this.lexico.getNextToken();
            compara("closed_square_bracket");
            addSymbol(this.token.getLexema(), tipoArray());
            this.token = this.lexico.getNextToken();
        } else {
            listaIdentificadores();
        }
    }

    private void tipoVariable() {
        if (tipoCoincideCon("int", "float", "boolean")) {
            this.tipo = this.token.getEtiqueta();
            this.token = this.lexico.getNextToken();
        } else {
            error("type (int, float, boolean)");
        }
    }

    private void listaIdentificadores() {
        String id = this.token.getLexema();
        addSymbol(id ,this.tipo);
        this.token = this.lexico.getNextToken();

        asignacionDeclaracion(id);
        masIdentificadores();
    }

    private void masIdentificadores() {
        if (tipoCoincideCon("comma")) {
          compara("comma");
          listaIdentificadores();
        }
    }

    private void asignacionDeclaracion(String id) {
        if (tipoCoincideCon("assignment")) {
            pila.lvalue(id);
            compara("assignment");
            String tipoRight = expresionLogica();
            pila.operator("=");
            if (!tipoRight.equals(this.tipo)) {
                reportError("asignando valor de tipo " + tipoRight + " a variable de tipo " + this.tipo);
            }
        }
    }

    private void instrucciones() {
        if (tipoCoincideCon("boolean", "do", "float", "id", "if", "int", "print", "while", "open_bracket")) {
            instruccion();
            instrucciones();
        }
    }

    private void instruccion() {
        switch (this.token.getEtiqueta()) {
            case "boolean", "int", "float" -> {
                declaracionVariable();
            }
            case "id" -> {
                pila.lvalue(this.token.getLexema());
                String tipoLeft = variable();
                compara("assignment");
                String tipoRight = expresionLogica();
                pila.operator("=");
                if (tipoLeft != null && tipoRight != null && !tipoRight.equals(tipoLeft)) {
                    reportError("asignando valor de tipo " + tipoRight + " a variable de tipo " + tipoLeft);
                }
                compara("semicolon");
            }
            case "if" -> {
                compara("if");
                compara("open_parenthesis");
                expresionLogica();
                compara("closed_parenthesis");

                int label1 = pila.lastLabel() + 1;
                pila.gofalse(label1);

                instruccion();
                elseOpcional(label1);
            }
            case "while" -> {
                int label = pila.lastLabel() + 1;
                pila.label(label);
                compara("while");
                compara("open_parenthesis");
                expresionLogica();
                compara("closed_parenthesis");
                pila.gofalse(label + 1);
                instruccion();
                pila.goto_(label);
                pila.label(label + 1);
            }
            case "do" -> {
                int label = pila.lastLabel() + 1;
                pila.label(label);
                compara("do");
                instruccion();
                compara("while");
                compara("open_parenthesis");
                expresionLogica();
                compara("closed_parenthesis");
                pila.gotrue(label);
                compara("semicolon");
            }
            case "print" -> {
                compara("print");
                compara("open_parenthesis");
                pila.print(this.token.getLexema());
                variable();
                compara("closed_parenthesis");
                compara("semicolon");
            }
            case "open_bracket" -> {
                compara("open_bracket");
                instrucciones();
                compara("closed_bracket");
            }
        }
    }

    private void elseOpcional(int label) {
        if (tipoCoincideCon("else")) {
            int label2 = pila.lastLabel() + 1;
            pila.goto_(label2);
            compara("else");
            pila.label(label);
            instruccion();
            pila.label(label2);
        } else {
            pila.label(label);
        }
    }

    private String variable() {
        if (tipoCoincideCon("id")) {
            String id = this.token.getLexema();
            String tipo;
            if (!this.simbolos.containsKey(id)) {
                reportError("identificador '" + id + "' no declarado");
                tipo = null;
            } else {
                tipo = this.simbolos.get(id);
            }
            this.token = this.lexico.getNextToken();
            arrayOpcional();
            return tipo;
        } else {
            error("id");
            return null;
        }
    }

    private void arrayOpcional() {
        if (tipoCoincideCon("open_square_bracket")) {
            compara("open_square_bracket");
            expresion();
            compara("closed_square_bracket");
        }
    }

    private String expresionLogica() {
        String tipo = terminoLogico();
        if (expresionLogicaPrima() == null) {
            return tipo;
        } else {
            return "boolean";
        }
    }

    private String expresionLogicaPrima() {
        if (tipoCoincideCon("or")) {
            compara("or");
            terminoLogico();
            expresionLogicaPrima();
            pila.operator("||");
            return "boolean";
        }
        return null;
    }

    private String terminoLogico() {
        String tipo = factorLogico();
        if (terminoLogicoPrima() == null) {
            return tipo;
        } else {
            return "boolean";
        }
    }

    private String terminoLogicoPrima() {
        if (tipoCoincideCon("and")) {
            compara("and");
            factorLogico();
            terminoLogicoPrima();
            pila.operator("&&");
            return "boolean";
        }
        return null;
    }

    private String factorLogico() {
        switch (this.token.getEtiqueta()) {
            case "not" -> {
                compara("not");
                factorLogico();
                pila.operator("!");
                return "boolean";
            }
            case "true" -> {
                compara("true");
                pila.push("true");
                return "boolean";
            }
            case "false" -> {
                compara("false");
                pila.push("false");
                return "boolean";
            }
            default -> {
                return expresionRelacional();
            }
        }
    }

    private String expresionRelacional() {
        String tipo = expresion();
        return operacionRelacionalOpcional(tipo);
    }

    private String operacionRelacionalOpcional(String tipoAnterior) {
        if (tipoCoincideCon("less_than", "less_equals", "greater_than", "greater_equals", "equals", "not_equals")) {
            String op = this.token.getLexema();
            operadorRelacional();
            expresion();
            pila.operator(op);
            return "boolean";
        }
        return tipoAnterior;
    }

    private void operadorRelacional() {
        switch (this.token.getEtiqueta()) {
            case "less_than" -> compara("less_than");
            case "less_equals" -> compara("less_equals");
            case "greater_than" -> compara("greater_than");
            case "greater_equals" -> compara("greater_equals");
            case "equals" -> compara("equals");
            case "not_equals" -> compara("not_equals");
            default -> error("relational operator (<=, ==, ...)");
        }
    }

    private String expresion() {
        String tipo = termino();
        return expresionPrima(tipo);
    }

    private String expresionPrima(String tipoAnterior) {
        if (tipoCoincideCon("add")) {
            compara("add");
            String tipo = termino();
            pila.operator("+");
            String tipoSumado = tipoSumado(tipoAnterior, tipo);
            return expresionPrima(tipoSumado);
        } else if (tipoCoincideCon("subtract")) {
            compara("subtract");
            String tipo = termino();
            pila.operator("-");
            String tipoSumado = tipoSumado(tipoAnterior, tipo);
            return expresionPrima(tipoSumado);
        }
        return tipoAnterior;
    }

    private String termino() {
        String tipo = factor();
        return terminoPrima(tipo);
    }

    private String terminoPrima(String tipoAnterior) {
        if (tipoCoincideCon("multiply", "divide", "remainder")) {
            String op = this.token.getLexema();
            this.token = this.lexico.getNextToken();
            String tipo = factor();
            pila.operator(op);
            String tipoSumado = tipoSumado(tipoAnterior, tipo);
            return terminoPrima(tipoSumado);
        }
        return tipoAnterior;
    }

    private String factor() {
        if (tipoCoincideCon("open_parenthesis")) {
            compara("open_parenthesis");
            String tipo = expresion();
            compara("closed_parenthesis");
            return tipo;
        } else if (tipoCoincideCon("id")) {
            pila.rvalue(this.token.getLexema());
            return variable();
        } else if (tipoCoincideCon("int", "float")) {
            String tipo = this.token.getEtiqueta();
            pila.push(this.token.getLexema());
            this.token = this.lexico.getNextToken();
            return tipo;
        } else {
            reportError("Expected number, id or expression, found " + this.token.getEtiqueta());
            this.token = this.lexico.getNextToken();
            return null;
        }
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
            error(token);
        }
    }

    private boolean tipoCoincideCon(String ...tipos) {
        for (String tipo : tipos) {
            if (this.token.getEtiqueta().equals(tipo)){
                return true;
            }
        }
        return false;
    }

    private String tipoArray() {
        return "array (" + this.tipo + ", " + this.tamanoArray + ")";
    }

    private void addSymbol(String id, String tipo) {
        if (this.simbolos.containsKey(id)) {
            System.out.println("Error en la linea " + this.lexico.getLineas() + ", identificador '" + id + "' ya declarado.");
            this.errores = true;
        } else {
            this.simbolos.put(id, tipo);
        }
    }

    private void error(String token) {
        System.out.println("Error en linea " + this.lexico.getLineas() + ". Expected " + token + ", Found " + this.token.getEtiqueta());
        this.errores = true;
    }

    private void reportError(String msg) {
        System.out.println("Error en linea " + this.lexico.getLineas() + ", " + msg);
        this.errores = true;
    }

    private String tipoSumado(String tipo1, String tipo2) {
        if (tipo1 == null) {
            return tipo2;
        }
        if (tipo2 == null) {
            return tipo1;
        }

        if (tipo1.equals("int") && tipo2.equals("int")) {
            return "int";
        } else if (tipo1.equals("float") && tipo2.equals("float")) {
            return "float";
        } else if (tipo1.equals("int") && tipo2.equals("float")) {
            return "float";
        } else if (tipo1.equals("float") && tipo2.equals("int")) {
            return "float";
        } else {
            return "error";
        }
    }

    private void codigoIntermedio() {
        String codigo = this.pila.toString();
        try {
            Files.writeString(Path.of("res/codigo.txt"), codigo);
        } catch (IOException e) {
            System.err.println("Error al crear archivo de codigo intermedio: " + e.getMessage());
        }
    }

}
