package nl.astraeus.template;

import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class ValuePart extends TemplatePart {

    private String [] parts;

    public ValuePart(int line, String text) {
        super(line);

        parts = text.split("\\.");
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        Object object = getValueFromModel(model, parts);

        if (object != null) {
            // get current output target and escape our result
            // result = escapedResult...

            result.append(String.valueOf(object));
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

}
