package nl.astraeus.template;

import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class PlainValuePart extends TemplatePart {

    private String [] parts;

    public PlainValuePart(int line, String text) {
        super(line);

        parts = text.split("\\.");
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        result.append(String.valueOf(getValueFromModel(model, parts)));
    }
}
