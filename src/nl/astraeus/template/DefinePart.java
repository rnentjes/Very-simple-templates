package nl.astraeus.template;

import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class DefinePart extends TemplatePart {

    private SimpleTemplate owner;
    private SimpleTemplate template;
    private String name;
    private String [] variables;

    public DefinePart(int line, SimpleTemplate owner, SimpleTemplate template, String name, String [] variables) {
        super(line);

        this.owner = owner;
        this.template = template;
        this.name = name;
        this.variables = variables;
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        owner.addDefine(name, this);
    }

    protected void renderCall(Map<String, Object> model, StringBuilder result) {
        template.render(model, result);
    }

    public String[] getVariables() {
        return variables;
    }
}
