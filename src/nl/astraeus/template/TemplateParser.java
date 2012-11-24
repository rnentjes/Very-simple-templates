package nl.astraeus.template;

import nl.astraeus.template.cache.TemplateCache;

import java.io.*;
import java.nio.charset.Charset;
import java.util.*;

/**
 * User: rnentjes
 * Date: 10/28/12
 * Time: 8:48 PM
 */
public class TemplateParser {

    private String startDelimiter, endDelimiter;
    private EscapeMode defaultEscapeMode = EscapeMode.NONE;
    private TemplatePart [] parts = null;

    private String template;
    private File resourceFile = null;
    private Class resourceClass = null;
    private String resourceLocation = null;
    private SimpleTemplate simpleTemplate;

    private static Map<String, DefinePart> defines = new HashMap<String, DefinePart>();

    public TemplateParser(SimpleTemplate simpleTemplate, String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, String template) {
        this.simpleTemplate = simpleTemplate;
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.defaultEscapeMode = defaultEscapeMode;
        this.template = template;

        parseTemplate(template);
    }

    public TemplateParser(SimpleTemplate simpleTemplate, String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, File resourceFile) {
        this.simpleTemplate = simpleTemplate;
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.defaultEscapeMode = defaultEscapeMode;
        this.resourceFile = resourceFile;
        this.template = readFile(resourceFile);

        parseTemplate(template);
    }

    public TemplateParser(SimpleTemplate simpleTemplate, String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, Class resourceClass, String resourceLocation) {
        this.simpleTemplate = simpleTemplate;
        this.startDelimiter = startDelimiter;
        this.endDelimiter = endDelimiter;
        this.defaultEscapeMode = defaultEscapeMode;
        this.resourceClass = resourceClass;
        this.resourceLocation = resourceLocation;
        this.template = readResource(resourceClass, resourceLocation);

        parseTemplate(template);
    }

    private String readFile(File file) {
        String result;
        try {
            InputStream in = new FileInputStream(file);

            result = readInputStream(in);

            in.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    private String readResource(Class resourceClass, String resourceLocation) {
        String result;
        try {
            InputStream in = resourceClass.getResourceAsStream(resourceLocation);

            result = readInputStream(in);

            in.close();
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return result;
    }

    private String readInputStream(InputStream in) throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        byte [] buffer = new byte[8196];
        int nr = 0;

        while((nr = in.read(buffer)) > 0) {
            out.write(buffer, 0, nr);
        }

        return new String(out.toByteArray(), Charset.forName("UTF-8"));
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
        Stack<DefinePart> currentDefinePart = new Stack<DefinePart>();
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
                case DATE:
                    stack.peek().add(new DateValuePart(currentEscapeMode.peek().getMode(), token.getLine(), token.getValue()));
                    break;
                case INCLUDE:
                    String [] incParts = getParameterFromCommand(token.getValue()).split("\\,");
                    String templateName = null;
                    String [] objectParts = new String[0];

                    if (incParts.length > 0) {
                        templateName = incParts[0].trim();
                    }

                    if (incParts.length == 2) {
                        objectParts = incParts[1].split(" as ");
                    }

                    SimpleTemplate tpl = null;

                    if (resourceFile != null) {
                        File file;
                        if (resourceFile.isDirectory()) {
                            file = new File(resourceFile, templateName);
                        } else {
                            file = new File(resourceFile.getParentFile(), templateName);
                        }
                        tpl = TemplateCache.getSimpleTemplate(file);
                    } else if (resourceClass != null) {
                        tpl = TemplateCache.getSimpleTemplate(resourceClass, templateName);
                    } else {
                        tpl = TemplateCache.getSimpleTemplate(new File(templateName));
                    }

                    if (objectParts.length == 2) {
                        stack.peek().add(new IncludePart(token.getLine(), tpl, objectParts[0].trim(), objectParts[1].trim()));
                    } else {
                        stack.peek().add(new IncludePart(token.getLine(), tpl));
                    }
                    break;
                case DEFINE:
                    String [] bothParts = getParameterFromCommand(token.getValue()).split("\\|");
                    String [] variableParts = new String[0];

                    if (bothParts.length != 2) {
                        throw new ParseException("Wrong number of arguments for define", token.getLine());
                    } else {
                        String variableName = bothParts[0].trim();

                        variableParts = bothParts[1].split("\\,");

                        stack.push(new ArrayList<TemplatePart>());

                        currentDefinePart.push(new DefinePart(token.getLine(), simpleTemplate, variableName, variableParts));
                    }
                    break;
                case ENDDEFINE:
                    currentDefinePart.peek().setParts(stack.pop());

                    defines.put(currentDefinePart.peek().getName(), currentDefinePart.pop());
                    break;
                case CALL:
                    String [] bothParamParts = getParameterFromCommand(token.getValue()).split("\\|");
                    String [] varParts = new String[0];

                    if (bothParamParts.length != 2) {
                        throw new ParseException("Wrong number of arguments for define", token.getLine());
                    } else {
                        String variableName = bothParamParts[0].trim();

                        varParts = bothParamParts[1].split("\\,");
                        DefinePart define = defines.get(variableName);

                        if (define == null) {
                            throw new ParseException("No define for call to "+variableName, token.getLine());
                        }

                        stack.peek().add(new CallPart(token.getLine(), define, variableName, varParts));
                    }
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
