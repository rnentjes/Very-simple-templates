package nl.astraeus.template;

import nl.astraeus.template.cache.CachedFormatters;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class DateTimeValuePart extends ValuePart {

    public DateTimeValuePart(EscapeMode mode, int line, String templateName, String text) {
        super(mode, line, templateName, text);
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
        Object object = getValueFromModel(model, parts);
        String value;


        if (object != null) {
            if (object instanceof Date) {
                value = CachedFormatters.getDateFormat("dd-MM-yyyy HH:mm").format(object);
            } else if (object.getClass().equals(long.class) || object.getClass().equals(Long.class)) {
                value = CachedFormatters.getDateFormat("dd-MM-yyyy HH:mm").format(new Date((Long)object));
            } else {
                throw new IllegalStateException("unknown date type "+object.getClass());
            }

            escape(value, result);
        } else {
            String partString = "";

            for (String p : parts) {
                if (partString.length() > 0) {
                    partString = partString + ".";
                }
                partString =  partString + p;
            }

            throw new RenderException("Can't retrieve value from model, model: "+model.get(parts[0])+", parts: "+partString, getFileName(), getLine());
        }
    }
}
