package nl.astraeus.template;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.Charset;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class PlainValuePart extends TemplatePart {
    private static Charset charset = Charset.forName("UTF-8");

    private String [] parts;
    private String text;

    public PlainValuePart(int line, String templateName, String text) {
        super(line, templateName);

        this.text = text;
        parts = text.split("\\.");
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
        result.write(String.valueOf(getValueFromModel(model, parts)).getBytes(charset));
    }

    public String getParameterName() {
        return text;
    }
}
