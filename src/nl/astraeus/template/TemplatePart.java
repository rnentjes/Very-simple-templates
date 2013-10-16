package nl.astraeus.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:54 PM
 */
public abstract class TemplatePart {

    private final int line;
    private final String template;

    protected TemplatePart(int line, String template) {
        this.line = line;
        this.template = template;
    }

    protected int getLine() {
        return line;
    }

    protected String getFileName() {
        return template;
    }

    public abstract void render(Map<String, Object> model, OutputStream result) throws IOException;

    protected void renderParts(TemplatePart [] parts, Map<String, Object> model, OutputStream out) throws IOException {
        for (TemplatePart part : parts) {
            part.render(model, out);
        }
    }

    protected Object getValueFromModel(Map<String, Object> model, String valueName) {
        String [] parts = valueName.split("\\.");

        return getValueFromModel(model, parts);
    }

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
                        value = ReflectHelper.get().getMethodValue(value, parts[index]);
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

            throw new RenderException("Can't retrieve value from model, model: "+model.get(parts[0])+", parts: "+partString, template, getLine());
        }

        return value;
    }
}
