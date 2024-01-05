package compilador.analisislexico;

public class Token {
    private final String etiqueta;
    private final String lexema;

    public Token(String etiqueta, String lexema) {
        this.etiqueta = etiqueta;
        this.lexema = lexema;
    }

    public Token(String etiqueta) {
        this.etiqueta = etiqueta;
        this.lexema = "";
    }

    public String getEtiqueta() {
        return etiqueta;
    }

    public String getLexema() {
        return lexema;
    }

    @Override
    public String toString() {
        if (lexema.isEmpty())
            return etiqueta;
        else
            return etiqueta + ", " + lexema;
    }
}