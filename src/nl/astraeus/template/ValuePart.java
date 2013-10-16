package nl.astraeus.template;

import java.io.IOException;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class ValuePart extends TemplatePart {

    private static Charset charset = Charset.forName("UTF-8");

    private static byte [] LESS_THEN    = "&lt;".getBytes(charset);
    private static byte [] GREATER_THEN = "&gt;".getBytes(charset);
    private static byte [] AMPERSAND    = "&amp;".getBytes(charset);
    private static byte [] DOUBLE_QUOTE = "&quot;".getBytes(charset);
    private static byte [] BACKSLASH    = "&#39;".getBytes(charset);
    private static byte [] BR           = "<br/>\n".getBytes(charset);

    protected String text;
    protected String [] parts;
    protected Method [] methods;
    protected EscapeMode mode;

    public ValuePart(EscapeMode mode, int line, String templateName, String text) {
        super(line, templateName);

        this.mode = mode;
        this.text = text;
        parts = text.split("\\.");
        methods = new Method[parts.length];
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
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

            throw new RenderException("Can't retrieve value from model, model: "+model.get(parts[0])+", parts: "+partString, getFileName(), getLine());
        }
    }

    @Override
    protected Object getValueFromModel(Map<String, Object> model, String [] parts) {
        int index = 0;
        Object value = null;

        try {
            if (parts.length > index) {
                value = model.get(parts[index]);

                while(value != null && parts.length > ++index) {
                    if (value instanceof Map) {
                        value = ((Map)value).get(parts[index]);
                    } else {
                        if (methods[index] == null) {
                            methods[index] = ReflectHelper.get().findGetMethod(value, parts[index]);

                            if (methods[index] == null) {
                                methods[index] = ReflectHelper.get().findIsMethod(value, parts[index]);
                            }
                        }

                        try {
                            if (methods[index] == null) {
                                value = ReflectHelper.get().getMethodValue(value, parts[index]);
                            } else {
                                value = methods[index].invoke(value);
                            }
                        } catch (IllegalAccessException e) {
                            value = ReflectHelper.get().getMethodValue(value, parts[index]);
                        } catch (InvocationTargetException e) {
                            value = ReflectHelper.get().getMethodValue(value, parts[index]);
                        }
                    }
                }
            }
        } catch (IllegalArgumentException e) {
            String partString = "";

            for (String p : parts) {
                if (partString.length() > 0) {
                    partString = partString + ".";
                }
                partString =  partString + p;
            }

            throw new RenderException("Can't retrieve value from model, model: "+model.get(parts[0])+", parts: "+partString, getFileName(), getLine());
        }

        return value;
    }

    protected void escape(String value, OutputStream out) throws IOException {
        switch(mode) {
            case HTML:
                escapeHtml(value, out);
                break;
            case HTMLBR:
                escapeHtmlBR(value, out);
                break;
            case JAVASCRIPT:
            case JSON:
            case XML:
                throw new IllegalStateException("Escape mode "+mode+" not supported yet");
            case NONE:
            default:
                out.write(value.getBytes(charset));
                break;
        }
    }

    protected void escapeHtml(String value, OutputStream out) throws IOException {
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);

            // &, <, >, ", ', `, , !, @, $, %, (, ), =, +, {, }, [, and ]
            // http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php

            switch(ch) {
                case '<':
                    out.write(LESS_THEN);
                    break;
                case '>':
                    out.write(GREATER_THEN);
                    break;
                case '&':
                    out.write(AMPERSAND);
                    break;
                case '"':
                    out.write(DOUBLE_QUOTE);
                    break;
                case '\'':
                    out.write(BACKSLASH);
                    break;
                default:
                    if (ch < 128) {
                        out.write((byte)ch);
                    } else {
                        out.write(new String(new char[] { ch }).getBytes(charset));
                    }
            }
        }

    }

    protected void escapeHtmlBR(String value, OutputStream out) throws IOException {
        for (int index = 0; index < value.length(); index++) {
            char ch = value.charAt(index);

            // &, <, >, ", ', `, , !, @, $, %, (, ), =, +, {, }, [, and ]
            // http://www.theukwebdesigncompany.com/articles/entity-escape-characters.php

            switch(ch) {
                case '<':
                    out.write(LESS_THEN);
                    break;
                case '>':
                    out.write(GREATER_THEN);
                    break;
                case '&':
                    out.write(AMPERSAND);
                    break;
                case '"':
                    out.write(DOUBLE_QUOTE);
                    break;
                case '\'':
                    out.write(BACKSLASH);
                    break;
                case '\n':
                    out.write(BR);
                    break;
                default:
                    if (ch < 128) {
                        out.write((byte)ch);
                    } else {
                        out.write(new String(new char[] { ch }).getBytes(charset));
                    }
            }
        }

    }

    public String getParameterName() {
        return text;
    }
}
