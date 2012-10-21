package nl.astraeus.template.cache;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 8/12/12
 * Time: 2:49 PM
 * <p/>
 * (c) Astreaeus B.V.
 */
public class TemplateCache {

    private static boolean developmentMode = false;

    public static void setDevelopmentMode(boolean mode) {
        developmentMode = mode;
    }

    public static boolean isDevelopmentMode() {
        return developmentMode;
    }

    private static Map<String, SimpleTemplate> templateCache = new HashMap<String, SimpleTemplate>();

    public synchronized static SimpleTemplate getSimpleTemplate(Class cls, String name) {
        String cacheKey = cls.getName()+"-"+name;

        SimpleTemplate result = templateCache.get(cacheKey);

        if (result == null || developmentMode) {
            InputStream in = null;

            try {
                in = cls.getResourceAsStream(name);

                result = SimpleTemplate.readTemplate(cls, "${","}", EscapeMode.HTML, in);

                templateCache.put(cacheKey, result);
            } catch (IOException e) {
                throw new IllegalStateException(e);
            } finally {
                if (in != null) {
                    try {
                        in.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        return result;
    }

}
