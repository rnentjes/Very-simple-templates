package nl.astraeus.template;

import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class StringPart extends TemplatePart {

    private String part;

    public StringPart(int line, String part) {
        super(line);

        this.part = part;
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        result.append(part);
    }
}
