import java.util.List;

public class Parser {
    private final List<Token> tokens;
    private int position = 0;

    public Parser(List<Token> tokens) {
        this.tokens = tokens;
    }

    // Şu anki token'a bak (tüketme)
    private Token peek() {
        if (position < tokens.size()) {
            return tokens.get(position);
        }
        return null;
    }

    // Token'ı tüket ve döndür
    private Token consume() {
        if (position < tokens.size()) {
            return tokens.get(position++);
        }
        return null;
    }

    // Belirli tip token'ı kontrol et ve tüket
    private boolean match(TokenType type) {
        Token token = peek();
        if (token != null && token.getType() == type) {
            consume();
            return true;
        }
        return false;
    }

    // Belirli değere sahip token'ı kontrol et ve tüket
    private boolean matchValue(String value) {
        Token token = peek();
        if (token != null && token.getValue().equals(value)) {
            consume();
            return true;
        }
        return false;
    }

    // Ana program ayrıştırma
    public boolean parseProgram() {
        parseStatementList();
        return position >= tokens.size(); // Tüm token'lar işlendi mi?
    }

    // İfade listesi ayrıştırma
    private void parseStatementList() {
        while (peek() != null && parseStatement()) {
            // İfadeleri ayrıştırmaya devam et
        }
    }

    // Tek bir ifade ayrıştırma
    private boolean parseStatement() {
        return parseDeclaration() || parseAssignment() || parseIf() || parseWhile() || parseBlock();
    }

    // Değişken tanımı: int x;
    private boolean parseDeclaration() {
        int start = position;
        if (matchValue("int") && match(TokenType.IDENTIFIER) && matchValue(";")) {
            return true;
        }
        position = start;
        return false;
    }

    // Atama işlemi: x = 10;
    private boolean parseAssignment() {
        int start = position;
        if (match(TokenType.IDENTIFIER) && matchValue("=") && parseExpression() && matchValue(";")) {
            return true;
        }
        position = start;
        return false;
    }

    // If ifadesi: if (condition) { ... }
    private boolean parseIf() {
        int start = position;
        if (matchValue("if") && matchValue("(") && parseExpression() && matchValue(")") && parseBlock()) {
            // Opsiyonel else
            if (peek() != null && peek().getValue().equals("else")) {
                consume();
                parseBlock();
            }
            return true;
        }
        position = start;
        return false;
    }

    // While döngüsü: while (condition) { ... }
    private boolean parseWhile() {
        int start = position;
        if (matchValue("while") && matchValue("(") && parseExpression() && matchValue(")") && parseBlock()) {
            return true;
        }
        position = start;
        return false;
    }

    // Blok: { ... }
    private boolean parseBlock() {
        int start = position;
        if (matchValue("{")) {
            parseStatementList();
            if (matchValue("}")) {
                return true;
            }
        }
        position = start;
        return false;
    }

    // Matematik ifadesi
    private boolean parseExpression() {
        if (parseTerm()) {
            // Opsiyonel operatör ve ikinci terim
            while (peek() != null && peek().getType() == TokenType.OPERATOR) {
                consume(); // Operatörü tüket
                if (!parseTerm()) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    // Terim (sayı veya değişken)
    private boolean parseTerm() {
        return parseFactor();
    }

    // Faktör (sayı, değişken veya parantez içi ifade)
    private boolean parseFactor() {
        Token token = peek();
        if (token == null) return false;

        // Sayı veya değişken
        if (token.getType() == TokenType.NUMBER || token.getType() == TokenType.IDENTIFIER) {
            consume();
            return true;
        }

        // Parantez içi ifade
        if (token.getType() == TokenType.SYMBOL && token.getValue().equals("(")) {
            consume();
            if (parseExpression() && matchValue(")")) {
                return true;
            }
        }

        return false;
    }

    // Test için main metodu
    public static void main(String[] args) {
        String code = "int x = 10;\nif (x > 5) {\n    x = x + 1;\n}";
        List<Token> tokens = Lexer.tokenize(code);
        
        System.out.println("Token'lar:");
        for (Token token : tokens) {
            System.out.println(token);
        }
        
        Parser parser = new Parser(tokens);
        if (parser.parseProgram()) {
            System.out.println("Ayrıştırma başarılı!");
        } else {
            System.out.println("Ayrıştırma başarısız.");
        }
    }
}