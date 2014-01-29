package nl.astraeus.template.cache;

import java.text.*;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * User: rnentjes
 * Date: 10/20/12
 * Time: 12:09 PM
 */
public class CachedFormatters {

    private static ThreadLocal<Map<String, DateFormat>> dateFormatters = new ThreadLocal<Map<String, DateFormat>>() {
        @Override
        protected Map<String, DateFormat> initialValue() {
            return new HashMap<String, DateFormat>();
        }
    };

    private static ThreadLocal<Map<String, NumberFormat>> amountFormatters = new ThreadLocal<Map<String, NumberFormat>>() {
        @Override
        protected Map<String, NumberFormat> initialValue() {
            return new HashMap<String, NumberFormat>();
        }
    };

    public static String formatNano(long l) {
        return getAmountFormat("###,##0.000000").format((double) l / 1000000.0);
    }

    public static DateFormat getDateFormat(String format) {
        DateFormat result = dateFormatters.get().get(format);

        if (result == null) {
            result = new SimpleDateFormat(format);

            dateFormatters.get().put(format, result);
        }

        return result;
    }

    public static DateFormat getUTCDateFormat(String format) {
        DateFormat result = dateFormatters.get().get("UTC"+format);

        if (result == null) {
            result = new SimpleDateFormat(format);
            result.setTimeZone(TimeZone.getTimeZone("UTC"));

            dateFormatters.get().put("UTC"+format, result);
        }

        return result;
    }

    public static NumberFormat getAmountFormat(String format) {
        NumberFormat result = amountFormatters.get().get(format);

        if (result == null) {
            result = new DecimalFormat(format, DecimalFormatSymbols.getInstance(new Locale("nl", "NL")));

            amountFormatters.get().put(format, result);
        }

        return result;
    }

}
