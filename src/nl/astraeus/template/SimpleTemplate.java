package nl.astraeus.template;

import nl.astraeus.template.cache.TemplateCache;

import java.io.*;
import java.nio.charset.Charset;
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
        return getTemplate("@", template);
    }

    public static SimpleTemplate getTemplate(String delimiter, String template) {
        return getTemplate(delimiter, delimiter, EscapeMode.NONE, template);
    }

    public static SimpleTemplate getTemplate(String delimiter, EscapeMode defaultEscapeMode, String template) {
        return getTemplate(delimiter, delimiter, defaultEscapeMode, template);
    }

    public static SimpleTemplate getTemplate(String startDelimiter, String endDelimiter, String template) {
        return getTemplate(startDelimiter,  endDelimiter, EscapeMode.NONE, template);
    }

    public static SimpleTemplate getTemplate(Class cls, String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, String template) {
        int hash = getHash(startDelimiter, endDelimiter, template);

        SimpleTemplate result = templateCache.get(hash);

        if (result == null) {
            result = new SimpleTemplate(cls, startDelimiter, endDelimiter, defaultEscapeMode, template);

            templateCache.put(hash, result);
        }

        return result;
    }

    public static SimpleTemplate getTemplate(String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, String template) {
        int hash = getHash(startDelimiter, endDelimiter, template);

        SimpleTemplate result = templateCache.get(hash);

        if (result == null) {
            result = new SimpleTemplate(startDelimiter, endDelimiter, defaultEscapeMode, template);

            templateCache.put(hash, result);
        }

        return result;
    }

    public static SimpleTemplate getTemplate(File file) throws IOException {
        return getTemplate("@", file);
    }

    public static SimpleTemplate getTemplate(String delimiter, File file) throws IOException {
        return getTemplate(delimiter, delimiter, file);
    }

    public static SimpleTemplate getTemplate(String startDelimiter, String endDelimiter, File file) throws IOException {
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

    private static int getHash(String ch1, String ch2, String st) {
        return ch1.hashCode() * 7 + ch2.hashCode() * 7 + st.hashCode();
    }

    public static SimpleTemplate readTemplate(InputStream in) throws IOException {
        return readTemplate("@", in);
    }

    public static SimpleTemplate readTemplate(String delimiter, InputStream in) throws IOException {
        return readTemplate(delimiter, delimiter, in);
    }

    public static SimpleTemplate readTemplate(String delimiter, EscapeMode defaultEscapeMode, InputStream in) throws IOException {
        return readTemplate(delimiter, delimiter, defaultEscapeMode, in);
    }

    public static SimpleTemplate readTemplate(String startDelimiter, String endDelimiter, InputStream in) throws IOException {
        return readTemplate(startDelimiter, endDelimiter, EscapeMode.NONE, in);
    }

    public static SimpleTemplate readTemplate(String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, InputStream in) throws IOException {
        String template = readInputStream(in);

        return getTemplate(startDelimiter, endDelimiter, defaultEscapeMode, template);
    }

    public static SimpleTemplate readTemplate(Class cls, String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, InputStream in) throws IOException {
        String template = readInputStream(in);

        return getTemplate(cls, startDelimiter, endDelimiter, defaultEscapeMode, template);
    }

    private static String readInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte [] buffer = new byte[8196];
        int nr = 0;

        while((nr = in.read(buffer)) > 0) {
            out.write(buffer, 0, nr);
        }

        return new String(out.toByteArray(), Charset.forName("UTF-8"));
    }

    private int hash;
    private String startDelimiter, endDelimiter;
    private EscapeMode defaultEscapeMode = EscapeMode.NONE;
    private TemplatePart [] parts;
    private Class resourceLocation = null;

    public SimpleTemplate(String template) {
        this("@", template);
    }

    public SimpleTemplate(EscapeMode defaultEscapeMode, String template) {
        this("@", defaultEscapeMode, template);
    }

    public SimpleTemplate(String delimiter, String template) {
        this(delimiter, delimiter, EscapeMode.NONE, template);
    }

    public SimpleTemplate(String delimiter, EscapeMode defaultEscapeMode, String template) {
        this(delimiter, delimiter, defaultEscapeMode, template);
    }

    public SimpleTemplate(String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, String template) {
        this(null, startDelimiter, endDelimiter, defaultEscapeMode, template);
    }

    public SimpleTemplate(Class cls, String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, String template) {
        this.resourceLocation = cls;
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.defaultEscapeMode = defaultEscapeMode;
        this.hash = getHash(startDelimiter, endDelimiter, template);

        parseTemplate(template);
    }

    public Class getResourceLocation() {
        return resourceLocation;
    }

    public void setResourceLocation(Class resourceLocation) {
        this.resourceLocation = resourceLocation;
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

    public TemplatePart [] getParts() {
        return parts;
    }

    private static class EscapeModeInfo {
        private EscapeMode mode;
        private int line;

        private EscapeModeInfo(EscapeMode mode, int line) {
            this.mode = mode;
            this.line = line;
        }

        public EscapeMode getMode() {
            return mode;
        }

        public int getLine() {
            return line;
        }
    }

    private void parseTemplate(String template) {
        TemplateTokenizer tokenizer = new TemplateTokenizer(startDelimiter, endDelimiter, template);

        List<TemplateToken> tokens = tokenizer.getTokens();
        Stack<List<TemplatePart>> stack = new Stack<List<TemplatePart>>();
        stack.push(new ArrayList<TemplatePart>());
        Stack<IfPart> currentIfPart = new Stack<IfPart>();
        Stack<ForEachPart> currentForEach = new Stack<ForEachPart>();
        Stack<EscapeModeInfo> currentEscapeMode = new Stack<EscapeModeInfo>();
        currentEscapeMode.push(new EscapeModeInfo(defaultEscapeMode, -1));

        for (TemplateToken token : tokens) {
            switch(token.getType()) {
                case ESCAPEHTML:
                    currentEscapeMode.push(new EscapeModeInfo(EscapeMode.HTML, token.getLine()));
                    break;
                case ESCAPEHTMLBR:
                    currentEscapeMode.push(new EscapeModeInfo(EscapeMode.HTMLBR, token.getLine()));
                    break;
                case ESCAPEJS:
                    currentEscapeMode.push(new EscapeModeInfo(EscapeMode.JAVASCRIPT, token.getLine()));
                    break;
                case ESCAPEXML:
                    currentEscapeMode.push(new EscapeModeInfo(EscapeMode.XML, token.getLine()));
                    break;
                case ESCAPENONE:
                    currentEscapeMode.push(new EscapeModeInfo(EscapeMode.NONE, token.getLine()));
                    break;
                case ESCAPEEND:
                    currentEscapeMode.pop();
                    break;
                case STRING:
                    stack.peek().add(new StringPart(token.getLine(), token.getValue()));
                    break;
                case VALUE:
                    stack.peek().add(new ValuePart(currentEscapeMode.peek().getMode(), token.getLine(), token.getValue()));
                    break;
                case PLAINVALUE:
                    stack.peek().add(new PlainValuePart(token.getLine(), token.getValue()));
                    break;
                case INCLUDE:
                    String templateName = token.getValue().substring("include(".length());
                    templateName = templateName.substring(0, templateName.length()-1);

                    SimpleTemplate tpl = null;

                    if (resourceLocation != null) {
                        tpl = TemplateCache.getSimpleTemplate(resourceLocation, templateName);
                    } else {
                        try {
                            tpl = getTemplate(startDelimiter, endDelimiter, new File(templateName));
                        } catch (IOException e) {
                            throw new IllegalStateException(e.getMessage(), e);
                        }
                    }

                    stack.peek().add(new IncludePart(token.getLine(), tpl));
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
                    currentForEach.peek().setIsMainPart();
                    break;
                case EACHMAIN:
                    currentForEach.peek().setCurrentParts(stack.pop());
                    currentForEach.peek().setIsMainPart();
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case EACHFIRST:
                    currentForEach.peek().setCurrentParts(stack.pop());
                    currentForEach.peek().setIsFirstPart();
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case EACHALT:
                    currentForEach.peek().setCurrentParts(stack.pop());
                    currentForEach.peek().setIsAltPart();
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case EACHLAST:
                    currentForEach.peek().setCurrentParts(stack.pop());
                    currentForEach.peek().setIsLastPart();
                    stack.push(new ArrayList<TemplatePart>());
                    break;
                case EACHEND:
                    currentForEach.peek().setCurrentParts(stack.pop());
                    stack.peek().add(currentForEach.pop());
                    break;
            }
        }

        List<TemplatePart> tmpParts = stack.pop();

        parts = tmpParts.toArray(new TemplatePart[tmpParts.size()]);

        if (stack.size() > 0) {
            if (currentForEach.size() > 0) {
                ForEachPart part = currentForEach.pop();

                throw new ParseException("Foreach not closed", part.getLine());
            } else if (currentIfPart.size() > 0) {
                IfPart part = currentIfPart.pop();

                throw new ParseException("If not closed", part.getLine());
            } else if (currentEscapeMode.size() > 1) {
                EscapeModeInfo modeInfo = currentEscapeMode.pop();

                throw new ParseException("Escape block not closed ("+modeInfo.getMode().toString().toLowerCase()+")", modeInfo.getLine());
            } else {
                List<TemplatePart> parts = stack.pop();

                if (!parts.isEmpty()) {
                    TemplatePart part = parts.get(0);

                    throw new ParseException("Template not parsed completely, last remaining part: "+part, part.getLine());
                } else {
                    throw new ParseException("Template not parsed completely, couldn't retrieve last remaining part (giving up)", -1);
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
