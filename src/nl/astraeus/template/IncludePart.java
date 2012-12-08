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
public class IncludePart extends TemplatePart {

    private SimpleTemplate template;
    private String [] modelParts = null;
    private String parameterName = null;

    public IncludePart(int line, SimpleTemplate template, String modelObject, String parameterName) {
        super(line);

        this.template = template;
        this.modelParts = modelObject.split("\\.");
        this.parameterName = parameterName;
    }

    public IncludePart(int line, SimpleTemplate template) {
        super(line);

        this.template = template;
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {

        if (modelParts != null && parameterName != null) {
            Object value = getValueFromModel(model, modelParts);

            Map<String, Object> tmpModel = new HashMap<String, Object>();
            tmpModel.put(parameterName, value);

            template.render(tmpModel, result);
        } else {
            template.render(model, result);
        }
    }
}
