package nl.astraeus.template;

import nl.astraeus.template.cache.CachedFormatters;

import java.io.IOException;
import java.io.OutputStream;
import java.math.BigDecimal;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/4/12
 * Time: 3:55 PM
 */
public class AmountValuePart extends ValuePart {

    public AmountValuePart(EscapeMode mode, int line, String templateName, String text) {
        super(mode, line, templateName, text);
    }

    @Override
    public void render(Map<String, Object> model, OutputStream result) throws IOException {
        Object object = getValueFromModel(model, parts);
        String value;

        if (object != null) {
            if (object.getClass().equals(double.class) || object.getClass().equals(Double.class)) {
                value = CachedFormatters.getAmountFormat("#,###,###,##0.00").format(object);
            } else if (object.getClass().equals(float.class) || object.getClass().equals(Float.class)) {
                value = CachedFormatters.getAmountFormat("#,###,###,##0.00").format(object);
            } else if (object.getClass().equals(long.class) || object.getClass().equals(Long.class)) {
                value = CachedFormatters.getAmountFormat("#,###,###,##0.00").format((((Long)object) / 100.0));
            } else if (object.getClass().equals(BigDecimal.class)) {
                value = CachedFormatters.getAmountFormat("#,###,###,##0.00").format(object);
            } else {
                throw new IllegalStateException("unknown amount type "+object.getClass());
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
