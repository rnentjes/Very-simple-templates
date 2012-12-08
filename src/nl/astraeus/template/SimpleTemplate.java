package nl.astraeus.template;

import java.io.*;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:52 PM
 */
public class SimpleTemplate {

    private Charset charset = Charset.forName("UTF-8");

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

    private static int getHash(String ch1, String ch2, File file) {
        try {
            return getHash(ch1, ch2, file.getCanonicalPath());
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }

    private static int getHash(String ch1, String ch2, Class resourceClass, String resourceLocation) {
        return getHash(ch1, ch2, resourceClass.getName() + ":" + resourceLocation);
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

    private Map<String, DefinePart> defines = new HashMap<String, DefinePart>();
    private SimpleTemplate parent = null;
    private TemplateParser parser;

    public SimpleTemplate(String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, File file) {
        hash = getHash(startDelimiter, endDelimiter, file);

        parser = new TemplateParser(this, startDelimiter, endDelimiter,  defaultEscapeMode, file);
    }

    public SimpleTemplate(String startDelimiter, String endDelimiter, EscapeMode defaultEscapeMode, Class resourceClass, String resourceLocation) {

        hash = getHash(startDelimiter, endDelimiter, resourceClass, resourceLocation);

        parser = new TemplateParser(this, startDelimiter, endDelimiter,  defaultEscapeMode, resourceClass, resourceLocation);
    }

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
        this.hash = getHash(startDelimiter, endDelimiter, template);

        parser = new TemplateParser(this, startDelimiter, endDelimiter,  defaultEscapeMode, template);
    }

    private void setParent(SimpleTemplate template) {
        parent = template;
    }

    public int getHash() {
        return hash;
    }

    public String render(Map<String, Object> model) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();

        try {
            render(model, out);
        } catch (IOException e) {
            throw new IllegalStateException(e);
        }

        return new String(out.toByteArray(), charset);
    }

    public void render(Map<String, Object> model, OutputStream out) throws IOException {
        for (TemplatePart part : parser.getParts()) {
            part.render(model, out);
        }
    }

    public void addDefine(String name, DefinePart definePart) {
        if(defines.get(name) != null) {
            System.err.println("Multiple defines for: "+name);
        }

        defines.put(name, definePart);
    }

    protected DefinePart getDefine(String name) {
        return defines.get(name);
    }
}
