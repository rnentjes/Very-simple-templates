package nl.astraeus.template;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class CallPart extends TemplatePart {

    private DefinePart define;
    private String name;
    private String [] variables;

    public CallPart(int line, String templateName, DefinePart define, String name, String[] variables) {
        super(line, templateName);

        this.define = define;
        this.name = name;
        this.variables = new String[variables.length];

        for (int index = 0; index < variables.length; index++) {
            this.variables[index] = variables[index].trim();
        }
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
        String [] defineVariables = define.getVariables();

        if (defineVariables.length != variables.length) {
//            throw new RenderException("Call and Define have different variable counts ("+name+")", getLine());
        }

        Map<String, Object> tmpModel = new HashMap<String, Object>();
        for (int index = 0; index < variables.length; index++) {
            Object value;
            String variable = variables[index];
            String name = defineVariables[index];

            if (variable.contains("=")) {
                String [] nameValue = variable.split("\\=");

                name = nameValue[0].trim();
                variable = nameValue[1].trim();
            }

            if (variable.startsWith("\"") && variable.endsWith("\"")) {
                value = variable.substring(1, variable.length() - 1);
            } else {
                value = getValueFromModel(model, variable.split("\\."));
            }

            tmpModel.put(name, value);
        }

        try {
            define.render(tmpModel, result);
        } catch (RenderException e) {
            RenderException caller = new RenderException(e, "Error calling  define \""+name+"\"", getFileName(), getLine());

            throw caller;
        }
    }
}
