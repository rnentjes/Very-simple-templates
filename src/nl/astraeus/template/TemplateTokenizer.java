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
        tokens = parseTemplateIntoTokens(startDelimiter, endDelimiter, template);
    }

    public List<TemplateToken> getTokens() {
        return tokens;
    }

    private List<TemplateToken> parseTemplateIntoTokens(char startDelimiter, char endDelimiter, String template) {
        List<TemplateToken> tokens = new ArrayList<TemplateToken>();
        StringBuilder current = new StringBuilder();

        boolean escape = false;
        boolean command = false;
        int line = 1;

        for (int index = 0; index < template.length(); index++) {
            char ch = template.charAt(index);

            if (ch == startDelimiter || ch == endDelimiter) {
                if (escape) {
                    current.append(ch);
                    escape = false;
                } else if (ch == endDelimiter && command) {
                    TokenType tokenType;
                    String tokenText = current.toString();

                    if (tokenText.startsWith("if(") && tokenText.endsWith(")")) {
                        tokenType = TokenType.IF;
                    } else if (tokenText.startsWith("ifnot(") && tokenText.endsWith(")")) {
                        tokenType = TokenType.IFNOT;
                    } else if (tokenText.equals("else")) {
                        tokenType = TokenType.ELSE;
                    } else if (tokenText.equals("endif")) {
                        tokenType = TokenType.ENDIF;
                    } else if (tokenText.equals("eachend") || tokenText.equals("endeach")) {
                        tokenType = TokenType.EACHEND;
                    } else if (tokenText.equals("eachalt") || tokenText.equals("lasteach")) {
                        tokenType = TokenType.EACHALT;
                    } else if (tokenText.equals("eachlast") || tokenText.equals("lasteach")) {
                        tokenType = TokenType.EACHLAST;
                    } else if (tokenText.startsWith("each(") && tokenText.endsWith(")")) {
                        tokenType = TokenType.EACH;
                    } else if (tokenText.startsWith("foreach(") && tokenText.endsWith(")")) {
                        tokenType = TokenType.EACH;
                    } else if (tokenText.startsWith("!")) {
                        tokenType = TokenType.PLAINVALUE;
                        tokenText = tokenText.substring(1);
                    } else {
                        tokenType = TokenType.VALUE;
                    }

                    tokens.add(new TemplateToken(tokenType, tokenText, line));
                    command = false;
                    current =  new StringBuilder();
                } else if (ch == startDelimiter) {
                    command = true;
                    tokens.add(new TemplateToken(TokenType.STRING, current.toString(), line));
                    current =  new StringBuilder();
                } else {
                    throw new ParseException("Unexpected delimiter '"+ch+"'", line);
                }

            } else {
                switch(ch) {
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
            }
        }

        if (current.length() > 0) {
            tokens.add(new TemplateToken(TokenType.STRING, current.toString(), line));
        }
        return tokens;
    }
}
