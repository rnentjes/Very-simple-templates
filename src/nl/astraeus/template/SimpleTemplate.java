package nl.astraeus.template;

import java.io.*;
import java.util.*;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:52 PM
 */
public class SimpleTemplate {

    private static Map<Integer, SimpleTemplate> templateCache = new HashMap<Integer, SimpleTemplate>();

    public static SimpleTemplate getTemplate(int hash) {
        return templateCache.get(hash);
    }

    public static SimpleTemplate getTemplate(String template) {
        return getTemplate('@', template);
    }

    public static SimpleTemplate getTemplate(char delimiter, String template) {
        return getTemplate(delimiter, delimiter, template);
    }

    public static SimpleTemplate getTemplate(char startDelimiter, char endDelimiter, String template) {
        int hash = getHash(startDelimiter, endDelimiter, template);

        SimpleTemplate result = templateCache.get(hash);

        if (result == null) {
            result = new SimpleTemplate(startDelimiter, endDelimiter, template);

            templateCache.put(hash, result);
        }

        return result;
    }

    public static SimpleTemplate getTemplate(File file) throws IOException {
        return getTemplate('@', file);
    }

    public static SimpleTemplate getTemplate(char delimiter, File file) throws IOException {
        return getTemplate(delimiter, delimiter, file);
    }

    public static SimpleTemplate getTemplate(char startDelimiter, char endDelimiter, File file) throws IOException {
        SimpleTemplate result = null;
        InputStream in = null;

        try {
            in = new FileInputStream(file);

            result = readTemplate(startDelimiter, endDelimiter, in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return result;
    }

    private static int getHash(char ch1, char ch2, String st) {
        return ch1 * 7 + ch2 * 7 + st.hashCode();
    }

    public static SimpleTemplate readTemplate(InputStream in) throws IOException {
        return readTemplate('@', in);
    }

    public static SimpleTemplate readTemplate(char delimiter, InputStream in) throws IOException {
        return readTemplate(delimiter, delimiter, in);
    }

    public static SimpleTemplate readTemplate(char startDelimiter, char endDelimiter, InputStream in) throws IOException {
        String template = readInputStream(in);

        return getTemplate(startDelimiter, endDelimiter, template);
    }

    private static String readInputStream(InputStream in) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        StringBuilder buffer = new StringBuilder();

        while(reader.ready()) {
            buffer.append(reader.readLine());
            buffer.append("\n");
        }

        return buffer.toString();
    }

    private int hash;
    private char startDelimiter, endDelimiter;
    private List<TemplatePart> parts = new ArrayList<TemplatePart>();

    public SimpleTemplate(String template) {
        this('@', template);
    }

    public SimpleTemplate(char delimiter, String template) {
        this(delimiter, delimiter, template);
    }

    public SimpleTemplate(char startDelimiter, char endDelimiter, String template) {
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.hash = getHash(startDelimiter, endDelimiter, template);

        parseTemplate(template);
    }

    public int getHash() {
        return hash;
    }

    public String render(Map<String, Object> model) {
        StringBuilder result = new StringBuilder();

        render(model, result);

        return result.toString();
    }

    public void render(Map<String, Object> model, StringBuilder result) {
        for (TemplatePart part : parts) {
            part.render(model, result);
        }
    }

    public List<TemplatePart> getParts() {
        return parts;
    }

    private void parseTemplate(String template) {
        TemplateTokenizer tokenizer = new TemplateTokenizer(startDelimiter, endDelimiter, template);

        List<TemplateToken> tokens = tokenizer.getTokens();
        Stack<List<TemplatePart>> stack = new Stack<List<TemplatePart>>();
        stack.push(new ArrayList<TemplatePart>());
        Stack<IfPart> currentIfPart = new Stack<IfPart>();
        Stack<ForEachPart> currentForEach = new Stack<ForEachPart>();
        EscapeMode currentEscapeMode = EscapeMode.NONE;

        for (TemplateToken token : tokens) {
            switch(token.getType()) {
                case ESCAPEHTML:
                    currentEscapeMode = EscapeMode.HTML;
                    break;
                case ESCAPENONE:
                    currentEscapeMode = EscapeMode.NONE;
                    break;
                case STRING:
                    stack.peek().add(new StringPart(token.getLine(), token.getValue()));
                    break;
                case VALUE:
                    stack.peek().add(new ValuePart(currentEscapeMode, token.getLine(), token.getValue()));
                    break;
                case PLAINVALUE:
                    stack.peek().add(new PlainValuePart(token.getLine(), token.getValue()));
                    break;
                case IF:
                    stack.push(new ArrayList<TemplatePart>());
                    currentIfPart.push(new IfPart(token.getLine(), getParameterFromCommand(token.getValue())));
                    break;
                case IFNOT:
                    stack.push(new ArrayList<TemplatePart>());
                    currentIfPart.push(new IfNotPart(token.getLine(), getParameterFromCommand(token.getValue())));
                    break;
                case ELSE:
                    currentIfPart.peek().setIfParts(stack.pop());
                    currentIfPart.peek().setHasElse(true);
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case ENDIF:
                    if (currentIfPart.peek().isHasElse()) {
                        currentIfPart.peek().setElseParts(stack.pop());
                    } else {
                        currentIfPart.peek().setIfParts(stack.pop());
                    }
                    stack.peek().add(currentIfPart.pop());
                    break;
                case EACH:
                    stack.push(new ArrayList<TemplatePart>());
                    String [] parts = getParameterFromCommand(token.getValue()).split(" as ");
                    if (parts.length != 2) {
                        throw new ParseException("Can't parse foreach expression, eg (persons as person)", token.getLine());
                    }
                    currentForEach.push(new ForEachPart(token.getLine(), parts[0], parts[1]));
                    break;
                case EACHALT:
                    currentForEach.peek().setHasAlt(true);
                    currentForEach.peek().setParts(stack.pop());
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case EACHLAST:
                    currentForEach.peek().setHasLast(true);
                    if (currentForEach.peek().isHasAlt()) {
                        currentForEach.peek().setAltParts(stack.pop());
                    } else {
                        currentForEach.peek().setParts(stack.pop());
                    }
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case EACHEND:
                    if (currentForEach.peek().isHasLast()) {
                        currentForEach.peek().setLastParts(stack.pop());
                    } else if (currentForEach.peek().isHasAlt()) {
                        currentForEach.peek().setAltParts(stack.pop());
                    } else {
                        currentForEach.peek().setParts(stack.pop());
                    }
                    stack.peek().add(currentForEach.pop());
                    break;
            }
        }

        parts = stack.pop();

        if (stack.size() > 0) {
            if (currentForEach.size() > 0) {
                ForEachPart part = currentForEach.pop();

                throw new ParseException("Foreach not closed", part.getLine());
            } else if (currentIfPart.size() > 0) {
                IfPart part = currentIfPart.pop();

                throw new ParseException("If not closed", part.getLine());
            } else {
                List<TemplatePart> parts = stack.pop();

                if (!parts.isEmpty()) {
                    TemplatePart part = parts.get(0);

                    throw new ParseException("Template not parsed completely, last remaining part: "+part, part.getLine());
                } else {
                    throw new ParseException("Template not parsed completely, coulnd't retrieve last remaining part (giving up)", -1);
                }
            }
        }
    }

    private String getParameterFromCommand(String command) {
        String [] parts = command.split("\\(");

        assert parts.length > 1;

        String [] parts2 = parts[1].split("\\)");

        assert parts2.length > 0;

        return parts2[0];
    }

}
