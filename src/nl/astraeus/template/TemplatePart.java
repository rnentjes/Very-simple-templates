package nl.astraeus.template;

import java.util.List;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:54 PM
 */
public abstract class TemplatePart {

    private int line;

    protected TemplatePart(int line) {
        this.line = line;
    }

    protected int getLine() {
        return line;
    }

    public abstract void render(Map<String, Object> model, StringBuilder result);

    protected void renderParts(List<TemplatePart> parts, Map<String, Object> model, StringBuilder result) {
        for (TemplatePart part : parts) {
            part.render(model, result);
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
                    value = ReflectHelper.get().getMethodValue(value, parts[index]);
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

            throw new RenderException("Can't retrieve value from model, model: "+model.get(parts[0])+", parts: "+partString, getLine());
        }

        return value;

    }
}
