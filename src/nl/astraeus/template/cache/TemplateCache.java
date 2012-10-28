package nl.astraeus.template.cache;

import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.SimpleTemplate;

import java.io.File;
import java.io.IOException;
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

    public synchronized static SimpleTemplate getSimpleTemplate(File file) {
        try {
            String cacheKey = file.getCanonicalPath();

            SimpleTemplate result = templateCache.get(cacheKey);

            if (result == null || developmentMode) {
                result = new SimpleTemplate("${","}", EscapeMode.HTML, file);
            }

            return result;
        } catch(IOException e) {
            throw new IllegalStateException(e);
        }
    }

    public synchronized static SimpleTemplate getSimpleTemplate(Class cls, String name) {
        String cacheKey = cls.getName()+"-"+name;

        SimpleTemplate result = templateCache.get(cacheKey);

        if (result == null || developmentMode) {
            result = new SimpleTemplate("${","}", EscapeMode.HTML, cls, name);
        }

        return result;
    }

}
