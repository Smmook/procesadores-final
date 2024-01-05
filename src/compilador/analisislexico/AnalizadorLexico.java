package compilador.analisislexico;

// Clase escaner
public class AnalizadorLexico {

    private final String texto;
    private final PalabrasReservadas keywords;
    private int posicion;
    private int lineas;
    private char caracter;

    public AnalizadorLexico(String texto) {
        this.texto = texto + '\0';
        this.keywords = new PalabrasReservadas("res/lexico.txt");
        this.posicion = 0;
        this.lineas = 1;
        nextChar();
    }

    public int getLineas() {
        return lineas;
    }

    private boolean esElFinal() {
        return posicion >= texto.length();
    }

    private void nextChar() {
        this.caracter = this.texto.charAt(posicion);
        posicion++;
    }

    private char checkNextChar() {
        return texto.charAt(posicion);
    }

    private boolean esNoSignificativo(char c) {
        return (c == ' ' || c == '\t' || c == '\r' || c == '\n');
    }

    private void nextCharSignificativo() {
        while (esNoSignificativo(caracter)) {
            if (caracter == '\n') {
                lineas++;
            }
            nextChar();
        }
    }

    private void checkComment() {
        if (caracter == '/' && checkNextChar() == '/') {
            do {
                nextChar();
                if (esElFinal()) return;
            } while (caracter != '\n');
            lineas++;
            nextCharSignificativo();
            return;
        }

        if (caracter == '/' && checkNextChar() == '*') {
            do {
                nextChar();
                if (caracter == '\n') {
                    lineas++;
                } else if (caracter == '\0') {
                    throw new RuntimeException("Comentario sin cerrar");
                }
            } while (!(caracter == '*' && checkNextChar() == '/'));
            nextChar();
            nextChar();

            nextCharSignificativo();
        }
    }

    private Token tokenNumero() {
        String numero = "";

        do {
            numero += caracter;
            nextChar();
        } while (Character.isDigit(caracter));

        if (caracter == '.' && Character.isDigit(checkNextChar())) {
            numero += caracter;
            nextChar();
            do {
                numero += caracter;
                nextChar();
            } while (Character.isDigit(caracter));

            return new Token("float", numero);
        } else {
            return new Token("int", numero);
        }
    }

    private Token tokenIdentificador() {
        String id = "";

        do {
            id += caracter;
            nextChar();
        } while (Character.isLetterOrDigit(caracter) || caracter == '_');

        String posibleTipo = keywords.getTipo(id);
        if (posibleTipo != null) {
            return new Token(posibleTipo, "");
        } else {
            return new Token("id", id);
        }
    }

    private Token tokenOperador() {
        String operador = "" + caracter;
        String posibleOperadorDoble = operador + checkNextChar();

        String posibleTipoOperadorDoble = keywords.getTipo(posibleOperadorDoble);
        if (posibleTipoOperadorDoble != null) {
            nextChar();
            nextChar();
            return new Token(posibleTipoOperadorDoble, posibleOperadorDoble);
        }

        String posibleTipoOperador = keywords.getTipo(operador);
        if (posibleTipoOperador != null) {
            nextChar();
            return new Token(posibleTipoOperador, operador);
        }

        throw new RuntimeException("Caracter no conocido");
    }

    public Token getNextToken() {
        nextCharSignificativo();

        checkComment();

        if (caracter == '\0') {
            return new Token("end_program");
        }

        if (Character.isDigit(caracter)) {
            return tokenNumero();
        }

        if (Character.isLetter(caracter)) {
            return tokenIdentificador();
        }

        return tokenOperador();
    }
}
