import java.util.*;
import java.util.regex.*;

public class Lexer {

    private static final Set<String> keywords = new HashSet<>(Arrays.asList(
        "int", "if", "else", "while", "return"
    ));

    private static final Pattern pattern = Pattern.compile(
        "\\s+|" +                              // boşluklar
        "\\b(int|if|else|while|return)\\b|" +  // anahtar kelimeler
        "\\b[a-zA-Z_][a-zA-Z0-9_]*\\b|" +      // tanımlayıcılar
        "\\d+|" +                              // sayılar
        "[=+\\-*/><!]=?|==|" +                 // operatörler
        "[(){};]"                              // semboller
);

    public static List<Token> tokenize(String code) {
        List<Token> tokens = new ArrayList<>();
        Matcher matcher = pattern.matcher(code);

        while (matcher.find()) {
            String lexeme = matcher.group();
            TokenType type;

            if (lexeme.matches("\\s+")) {
                type = TokenType.WHITESPACE;
            } else if (keywords.contains(lexeme)) {
                type = TokenType.KEYWORD;
            } else if (lexeme.matches("\\d+")) {
                type = TokenType.NUMBER;
            } else if (lexeme.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
                type = TokenType.IDENTIFIER;
            } else if (lexeme.matches("[=+\\-*/><!]=?|==")) {
                type = TokenType.OPERATOR;
            } else if (lexeme.matches("[(){};]")) {
                type = TokenType.SYMBOL;
            } else {
                type = TokenType.UNKNOWN;
            }

            if (type != TokenType.WHITESPACE) {
                tokens.add(new Token(lexeme, type));
            }
        }

        return tokens;
    }

    public static void main(String[] args) {
        String code = "while (count < 100) { count = count + 1; }";
        List<Token> tokens = tokenize(code);
        for (Token token : tokens) {
            System.out.println(token);
        }
    }
}
