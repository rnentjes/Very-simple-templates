package nl.astraeus.template;

import java.awt.event.InputEvent;
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

    public static SimpleTemplate getTemplate(char templateChar, String template) {
        int hash = getHash(templateChar, template);

        SimpleTemplate result = templateCache.get(hash);

        if (result == null) {
            result = new SimpleTemplate(templateChar, template);

            templateCache.put(hash, result);
        }

        return result;
    }

    public static SimpleTemplate getTemplate(File file) throws IOException {
        return getTemplate('@', file);
    }

    public static SimpleTemplate getTemplate(char templateChar, File file) throws IOException {
        SimpleTemplate result = null;
        InputStream in = null;

        try {
            in = new FileInputStream(file);

            result = readTemplate(templateChar, in);
        } finally {
            if (in != null) {
                in.close();
            }
        }

        return result;
    }

    private static int getHash(char ch, String st) {
        return ch * 7 + st.hashCode();
    }

    public static SimpleTemplate readTemplate(InputStream in) throws IOException {
        return readTemplate('@', in);
    }

    public static SimpleTemplate readTemplate(char templateChar, InputStream in) throws IOException {
        String template = readInputStream(in);

        return getTemplate(templateChar, template);
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
    private char templateChar;
    private List<TemplatePart> parts = new ArrayList<TemplatePart>();

    public SimpleTemplate(String template) {
        this('@', template);
    }

    public SimpleTemplate(char templateChar, String template) {
        this.templateChar = templateChar;
        this.hash = getHash(templateChar, template);

        parseTemplate(template);
    }

    public int getHash() {
        return hash;
    }

    public String render(Map<String, Object> model) {
        StringBuilder result = new StringBuilder();

        for (TemplatePart part : parts) {
            result.append(part.render(model));
        }

        return result.toString();
    }

    public List<TemplatePart> getParts() {
        return parts;
    }

    private void parseTemplate(String template) {
        TemplateTokenizer tokenizer = new TemplateTokenizer(templateChar, template);

        List<TemplateToken> tokens = tokenizer.getTokens();
        Stack<List<TemplatePart>> stack = new Stack<List<TemplatePart>>();
        stack.push(new ArrayList<TemplatePart>());
        Stack<IfPart> currentIfPart = new Stack<IfPart>();
        Stack<ForEachPart> currentForEach = new Stack<ForEachPart>();

        for (TemplateToken token : tokens) {
            switch(token.getType()) {
                case STRING:
                    stack.peek().add(new StringPart(token.getValue()));
                    break;
                case VALUE:
                    stack.peek().add(new ValuePart(token.getValue()));
                    break;
                case PLAINVALUE:
                    stack.peek().add(new PlainValuePart(token.getValue()));
                    break;
                case IF:
                    stack.push(new ArrayList<TemplatePart>());
                    currentIfPart.push(new IfPart(getParameterFromCommand(token.getValue())));
                    break;
                case IFNOT:
                    stack.push(new ArrayList<TemplatePart>());
                    currentIfPart.push(new IfNotPart(getParameterFromCommand(token.getValue())));
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
                    currentForEach.push(new ForEachPart(parts[0], parts[1]));
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

        assert parts.size() == 0;
    }

    private String getParameterFromCommand(String command) {
        String [] parts = command.split("\\(");

        assert parts.length > 1;

        String [] parts2 = parts[1].split("\\)");

        assert parts2.length > 0;

        return parts2[0];
    }

}
