package nl.astraeus.template;

import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class ValuePart extends TemplatePart {

    private String [] parts;
    private EscapeMode mode;

    public ValuePart(EscapeMode mode, int line, String text) {
        super(line);

        this.mode = mode;
        parts = text.split("\\.");
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        Object object = getValueFromModel(model, parts);

        if (object != null) {
            escape(String.valueOf(object), result);
        } else {
            String partString = "";

            for (String p : parts) {
                if (partString.length() > 0) {
                    partString = partString + ".";
                }
                partString =  partString + p;
            }

            throw new RenderException("Can't retrieve value from model, model: "+model.get(parts[0])+", parts: "+partString, getLine());
        }
    }

    private void escape(String value, StringBuilder result) {
        switch(mode) {
            case HTML:
                escapeHtml(value, result);
                break;
            case JAVASCRIPT:
            case JSON:
            case XML:
                throw new IllegalStateException("Escape mode "+mode+" not supported yet");
            case NONE:
            default:
                result.append(value);
                break;
        }
    }

    private void escapeHtml(String value, StringBuilder result) {
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);

            // &, <, >, ", ', `, , !, @, $, %, (, ), =, +, {, }, [, and ]
            // http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php

            switch(ch) {
                case '<':
                    result.append("&lt;");
                    break;
                case '>':
                    result.append("&gt;");
                    break;
                case '&':
                    result.append("&amp;");
                    break;
                case '"':
                    result.append("&quot;");
                    break;
                case '\'':
                    result.append("&#39;");
                    break;
                case ' ':
                    result.append("&nbsp;");
                    break;
                default:
                    result.append(ch);
            }
        }

    }

}
