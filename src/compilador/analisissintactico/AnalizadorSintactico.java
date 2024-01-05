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
    private boolean errores;

    public AnalizadorSintactico(AnalizadorLexico lexico) {
        this.lexico = lexico;
        this.token = this.lexico.getNextToken();
        this.simbolos = new Hashtable<>();
        this.errores = false;
    }

    public void analisis() {
        programa();
        if (!errores) {
            System.out.println("Programa compilado correctamente");
        }
    }

    private void programa() {
        compara("void");
        compara("main");
        compara("open_bracket");
        declaraciones();
        instrucciones();
        compara("closed_bracket");
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
            compara("open_square_bracket");
            this.tamanoArray = Integer.parseInt(this.token.getLexema());
            this.token = this.lexico.getNextToken();
            compara("closed_square_bracket");
            this.simbolos.put(this.token.getLexema(), tipoArray());
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
        this.simbolos.put(id, this.tipo);
        // TODO: 05/01/2024 Hacer algo con el id
        this.token = this.lexico.getNextToken();
        asignacionDeclaracion();
        masIdentificadores();
    }

    private void masIdentificadores() {
        if (tipoCoincideCon("comma")) {
          compara("comma");
          listaIdentificadores();
        }
    }

    private void asignacionDeclaracion() {
        if (tipoCoincideCon("assignment")) {
            compara("assignment");
            expresionLogica();
        }
    }

    private void instrucciones() {
        if (tipoCoincideCon("boolean", "do", "float", "id", "if", "int", "print", "while", "open_bracket")) {
            instruccion();
            instrucciones();
        }
    }

    private void instruccion() {
        // TODO: 05/01/2024 La mas gorda
    }

    private void elseOpcional() {
        if (tipoCoincideCon("else")) {
            compara("else");
            instruccion();
        }
    }

    private void variable() {
        if (tipoCoincideCon("id")) {
            String id = this.token.getLexema();
            this.token = this.lexico.getNextToken();
            arrayOpcional();
        } else {
            error("id");
        }
    }

    private void arrayOpcional() {
        if (tipoCoincideCon("open_square_bracket")) {
            compara("open_square_bracket");
            expresion();
            compara("closed_square_bracket");
        }
    }

    private void expresionLogica() {
        terminoLogico();
        expresionLogicaPrima();
    }

    private void expresionLogicaPrima() {
        if (tipoCoincideCon("or")) {
            compara("or");
            terminoLogico();
            expresionLogicaPrima();
        }
    }

    private void terminoLogico() {
        factorLogico();
        terminoLogicoPrima();
    }

    private void terminoLogicoPrima() {
        if (tipoCoincideCon("and")) {
            compara("and");
            factorLogico();
            terminoLogicoPrima();
        }
    }

    private void factorLogico() {
        switch (this.token.getEtiqueta()) {
            case "not" -> {
                compara("not");
                factorLogico();
            }
            case "true" -> {
                compara("true");
            }
            case "false" -> {
                compara("false");
            }
            default -> {
                expresionRelacional();
            }
        }
    }

    private void expresionRelacional() {
        expresion();
        operacionRelacionalOpcional();
    }

    private void operacionRelacionalOpcional() {
        if (tipoCoincideCon("less_than", "less_equals", "greater_than", "greater_equals", "equals", "not_equals")) {
            operadorRelacional();
            expresion();
        }
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

    private void expresion() {
        termino();
        expresionPrima();
    }

    private void expresionPrima() {
        if (tipoCoincideCon("add")) {
            compara("add");
            termino();
            expresionPrima();
        } else if (tipoCoincideCon("subtract")) {
            compara("subtract");
            termino();
            expresionPrima();
        }
    }

    private void termino() {
        factor();
        terminoPrima();
    }

    private void terminoPrima() {
        if (tipoCoincideCon("multiply", "divide", "remainder")) {
            this.token = this.lexico.getNextToken();
            factor();
            terminoPrima();
        }
    }

    private void factor() {
        if (tipoCoincideCon("open_parenthesis")) {
            compara("open_parenthesis");
            expresion();
            compara("closed_parenthesis");
        } else if (tipoCoincideCon("id")) {
            variable();
        } else {
            this.token = this.lexico.getNextToken();
            // TODO: 05/01/2024 num
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

    private void error(String token) {
        System.out.println("Error en linea " + this.lexico.getLineas() + ". Expected " + token + ", Found " + this.token.getEtiqueta());
        this.errores = true;
    }

}
