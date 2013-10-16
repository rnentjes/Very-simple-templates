package nl.astraeus.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class DefinePart extends TemplatePart {

    private SimpleTemplate owner;
    private TemplatePart [] parts;
    private String name;
    private String [] variables;

    public DefinePart(int line, String templateName, SimpleTemplate owner, String name, String [] variables) {
        super(line, templateName);

        this.owner = owner;
        this.parts = new TemplatePart[0];
        this.name = name;
        this.variables = new String[variables.length];

        for (int index = 0; index < variables.length; index++) {
            this.variables[index] = variables[index].trim();
        }
    }

    public void setParts(List<TemplatePart> parts) {
        this.parts = parts.toArray(new TemplatePart[parts.size()]);
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
        for (TemplatePart part : parts) {
            part.render(model, result);
        }
    }

    public String[] getVariables() {
        return variables;
    }

    public String getName() {
        return name;
    }
}
