package nl.astraeus.template;

import java.util.ArrayList;
import java.util.List;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 5:11 PM
 */
public class TemplateTokenizer {

    private List<TemplateToken> tokens;

    public TemplateTokenizer(char startDelimiter, char endDelimiter, String template) {
        tokens = parseTemplateIntoTokens(String.valueOf(startDelimiter), String.valueOf(endDelimiter), template);
    }

    public TemplateTokenizer(String startDelimiter, String endDelimiter, String template) {
        tokens = parseTemplateIntoTokens(startDelimiter, endDelimiter, template);
    }

    public List<TemplateToken> getTokens() {
        return tokens;
    }

    private List<TemplateToken> parseTemplateIntoTokens(String startDelimiter, String endDelimiter, String template) {
        List<TemplateToken> tokens = new ArrayList<TemplateToken>();
        StringBuilder current = new StringBuilder();

        assert !startDelimiter.contains("\n");
        assert !startDelimiter.contains("\\");

        assert !endDelimiter.contains("\n");
        assert !endDelimiter.contains("\\");

        boolean escape = false;
        boolean command = false;
        boolean skip = false;
        int line = 1;

        int startCounter = 0;
        int endCounter = 0;

        for (int index = 0; index < template.length(); index++) {
            skip = false;
            char ch = template.charAt(index);

            if (!escape) {
                if (!command && startDelimiter.charAt(startCounter) == ch) {
                    startCounter++;
                    skip = true;

                    if (startCounter == startDelimiter.length()) {
                        // found command start
                        command = true;
                        tokens.add(new TemplateToken(TokenType.STRING, current.toString(), line));
                        current = new StringBuilder();
                        startCounter = 0;
                    }
                } else if (startCounter > 0) {
                    current.append(startDelimiter.substring(0, startCounter));

                    startCounter = 0;
                }

                if (!skip && endDelimiter.charAt(endCounter) == ch && command) {
                    endCounter++;
                    skip = true;

                    if (endCounter == endDelimiter.length()) {
                        TokenType tokenType;
                        String tokenText = current.toString();

                        if (tokenText.startsWith("if(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.IF;
                        } else if (tokenText.startsWith("ifnot(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.IFNOT;
                        } else if (tokenText.equals("else")) {
                            tokenType = TokenType.ELSE;
                        } else if (tokenText.equals("/if")) {
                            tokenType = TokenType.ENDIF;
                        } else if (tokenText.equals("escape(html)")) {
                            tokenType = TokenType.ESCAPEHTML;
                        } else if (tokenText.equals("escape(html|br)")) {
                            tokenType = TokenType.ESCAPEHTMLBR;
                        } else if (tokenText.equals("escape(js)")) {
                            tokenType = TokenType.ESCAPEJS;
                        } else if (tokenText.equals("escape(xml)")) {
                            tokenType = TokenType.ESCAPEXML;
                        } else if (tokenText.equals("escape(none)")) {
                            tokenType = TokenType.ESCAPENONE;
                        } else if (tokenText.equals("/escape")) {
                            tokenType = TokenType.ESCAPEEND;
                        } else if (tokenText.equals("/if")) {
                            tokenType = TokenType.ENDIF;
                        } else if (tokenText.equals("/each")) {
                            tokenType = TokenType.EACHEND;
                        } else if (tokenText.equals("eachalt")) {
                            tokenType = TokenType.EACHALT;
                        } else if (tokenText.equals("eachfirst")) {
                            tokenType = TokenType.EACHFIRST;
                        } else if (tokenText.equals("eachlast")) {
                            tokenType = TokenType.EACHLAST;
                        } else if (tokenText.equals("eachmain")) {
                            tokenType = TokenType.EACHMAIN;
                        } else if (tokenText.startsWith("each(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.EACH;
                        } else if (tokenText.startsWith("foreach(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.EACH;
                        } else if (tokenText.startsWith("include(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.INCLUDE;
                        } else if (tokenText.startsWith("define(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.DEFINE;
                        } else if (tokenText.startsWith("/define")) {
                            tokenType = TokenType.ENDDEFINE;
                        } else if (tokenText.startsWith("call(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.CALL;
                        } else if (tokenText.startsWith("date(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.DATE;
                        } else if (tokenText.startsWith("datetime(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.DATETIME;
                        } else if (tokenText.startsWith("time(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.TIME;
                        } else if (tokenText.startsWith("timestamp(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.TIMESTAMP;
                        } else if (tokenText.startsWith("amount(") && tokenText.endsWith(")")) {
                            tokenType = TokenType.AMOUNT;
                        } else if (tokenText.startsWith("!")) {
                            tokenType = TokenType.PLAINVALUE;
                            tokenText = tokenText.substring(1);
                        } else {
                            tokenType = TokenType.VALUE;
                        }

                        tokens.add(new TemplateToken(tokenType, tokenText, line));
                        command = false;
                        current = new StringBuilder();
                        endCounter = 0;
                    }
                } else if (endCounter > 0) {
                    current.append(endDelimiter.substring(0, endCounter));

                    endCounter = 0;
                }
            } else {
                current.append(ch);
                startCounter = 0;
                endCounter = 0;
                escape = false;
                skip = true;
            }

            if (!command && !skip) {
                switch (ch) {
                    case '\\':
                        if (escape) {
                            current.append("\\");
                            escape = false;
                        } else {
                            escape = true;
                        }
                        break;
                    case '\n':
                        line++;
                        current.append(ch);
                        break;
                    default:
                        current.append(ch);
                        break;
                }
            } else if (!skip) {
                current.append(ch);
            }
        }

        if (current.length() > 0) {
            tokens.add(new TemplateToken(TokenType.STRING, current.toString(), line));
        }

        return tokens;
    }
}
