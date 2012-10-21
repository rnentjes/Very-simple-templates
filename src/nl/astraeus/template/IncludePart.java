package nl.astraeus.template;

import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class IncludePart extends TemplatePart {

    private String [] parts;
    private SimpleTemplate template;

    public IncludePart(int line, SimpleTemplate template) {
        super(line);

        this.template = template;
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        template.render(model, result);
    }
}
