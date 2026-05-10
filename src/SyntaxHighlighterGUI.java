import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;
import java.util.List;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

public class SyntaxHighlighterGUI extends JFrame {

    private final JTextPane textPane;
    private final StyledDocument doc;
    private boolean isHighlighting = false; // Özyinelemeyi önlemek için bayrak

    public SyntaxHighlighterGUI() {
        setTitle("Gerçek Zamanlı Sözdizimi Vurgulayıcı");
        setSize(800, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null); // Pencereyi ortala

        // Metin alanını oluştur
        textPane = new JTextPane();
        textPane.setFont(new Font("Consolas", Font.PLAIN, 14));
        doc = textPane.getStyledDocument();

        // Kaydırma çubuğu ekle
        JScrollPane scrollPane = new JScrollPane(textPane);
        add(scrollPane, BorderLayout.CENTER);

        // Durum çubuğu ekle
        JLabel statusBar = new JLabel("Hazır - Kod yazmaya başlayın");
        statusBar.setBorder(BorderFactory.createLoweredBevelBorder());
        add(statusBar, BorderLayout.SOUTH);

        setupStyles();
        setupEventListeners();

        // Örnek metin ekle (listener'dan sonra)
        String ornekKod = "int sayi = 42;\nif (sayi > 10) {\n    sayi = sayi * 2;\n    while (sayi < 100) {\n        sayi = sayi + 5;\n    }\n}\nreturn sayi;";
        textPane.setText(ornekKod);

        // İlk vurgulamayı yap
        SwingUtilities.invokeLater(() -> highlight());

        setVisible(true);
    }

    // Her token tipi için stil ayarla
    private void setupStyles() {
        addStyle(TokenType.KEYWORD, Color.BLUE, false); // Mavi
        addStyle(TokenType.IDENTIFIER, Color.BLACK, false); // Siyah
        addStyle(TokenType.NUMBER, Color.MAGENTA, false); // Mor
        addStyle(TokenType.OPERATOR, Color.RED, false); // Kırmızı
        addStyle(TokenType.SYMBOL, Color.DARK_GRAY, false); // Koyu gri
        addStyle(TokenType.UNKNOWN, Color.GRAY, false); // Gri
    }

    private void addStyle(TokenType type, Color color, boolean bold) {
        Style style = textPane.addStyle(type.name(), null);
        StyleConstants.setForeground(style, color);
        StyleConstants.setBold(style, bold);
    }

    // Olay dinleyicilerini ayarla
    private void setupEventListeners() {
        // Döküman değişiklik dinleyicisi
        textPane.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) { 
                scheduleHighlight();
            }
            
            @Override
            public void removeUpdate(DocumentEvent e) { 
                scheduleHighlight();
            }
            
            @Override
            public void changedUpdate(DocumentEvent e) { 
                scheduleHighlight();
            }
        });
    }

    // Vurgulamayı gecikmeyle başlat, çok sık güncellemeleri önlemek için
    private Timer highlightTimer;
    
    private void scheduleHighlight() {
        if (isHighlighting) return; // Özyinelemeyi önle
        
        if (highlightTimer != null) {
            highlightTimer.stop();
        }
        
        highlightTimer = new Timer(100, e -> highlight()); // 100ms gecikme
        highlightTimer.setRepeats(false);
        highlightTimer.start();
    }

    // Ana vurgulama fonksiyonu
    private void highlight() {
        if (isHighlighting) return; // Özyinelemeyi önle
        
        isHighlighting = true;
        
        try {
            String text = textPane.getText();
            if (text.isEmpty()) return;
            
            // İmleç konumunu kaydet
            int caretPosition = textPane.getCaretPosition();
            
            // Token'ları al
            List<Token> tokens = Lexer.tokenize(text);
            
            // Tüm stilleri temizle
            Style defaultStyle = textPane.getStyle(TokenType.IDENTIFIER.name());
            doc.setCharacterAttributes(0, text.length(), defaultStyle, true);
            
            // Her token için vurgulamayı uygula
            int textPosition = 0;
            
            for (Token token : tokens) {
                String tokenValue = token.getValue();
                TokenType tokenType = token.getType();
                
                // Bir sonraki token'a kadar metindeki boşlukları atla
                while (textPosition < text.length() && 
                       Character.isWhitespace(text.charAt(textPosition))) {
                    textPosition++;
                }
                
                // Token'ın mevcut konumla eşleşip eşleşmediğini kontrol et
                if (textPosition + tokenValue.length() <= text.length()) {
                    String textSubstring = text.substring(textPosition, textPosition + tokenValue.length());
                    
                    if (textSubstring.equals(tokenValue)) {
                        // Token'a stil uygula
                        Style style = textPane.getStyle(tokenType.name());
                        if (style != null) {
                            doc.setCharacterAttributes(textPosition, tokenValue.length(), style, true);
                        }
                        textPosition += tokenValue.length();
                    } else {
                        // Token eşleşmezse, kalan metinde ara
                        int foundPos = text.indexOf(tokenValue, textPosition);
                        if (foundPos != -1) {
                            Style style = textPane.getStyle(tokenType.name());
                            if (style != null) {
                                doc.setCharacterAttributes(foundPos, tokenValue.length(), style, true);
                            }
                            textPosition = foundPos + tokenValue.length();
                        }
                    }
                }
            }
            
            // İmleç konumunu geri yükle
            try {
                textPane.setCaretPosition(Math.min(caretPosition, text.length()));
            } catch (IllegalArgumentException e) {
                // İmleç konumlandırma hatalarını yoksay
            }
            
        } catch (Exception e) {
            System.err.println("Vurgulama hatası: " + e.getMessage());
            e.printStackTrace();
        } finally {
            isHighlighting = false;
        }
    }

    public static void main(String[] args) {
        // Sistem görünümünü kullan
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception e) {
            // Varsayılan görünümü kullan
        }
        
        SwingUtilities.invokeLater(() -> new SyntaxHighlighterGUI());
    }
}