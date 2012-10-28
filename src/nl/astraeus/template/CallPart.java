package nl.astraeus.template;

import java.util.HashMap;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class CallPart extends TemplatePart {

    private SimpleTemplate owner;
    private String name;
    private String [] variables;

    public CallPart(int line, SimpleTemplate owner, String name, String[] variables) {
        super(line);

        this.owner = owner;
        this.name = name;
        this.variables = variables;
    }

    @Override
    public void render(Map<String, Object> model, StringBuilder result) {
        DefinePart define = owner.getDefine(name);

        String [] defineVariables = define.getVariables();

        if (defineVariables.length != variables.length) {
            throw new RenderException("Call and Define have different variable counts ("+name+")", getLine());
        }

        Map<String, Object> tmpModel = new HashMap<String, Object>();
        for (int index = 0; index < variables.length; index++) {
            Object value;
            String variable = variables[index];

            if (variable.startsWith("\"") && variable.endsWith("\"")) {
                value = variable.substring(1, variable.length() - 1);
            } else {
                value = getValueFromModel(model, variable.split("\\."));
            }

            tmpModel.put(defineVariables[index], value);
        }

        define.renderCall(tmpModel, result);
    }
}
