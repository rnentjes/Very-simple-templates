package nl.astraeus.template;

import junit.framework.Assert;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/8/12
 * Time: 1:15 PM
 */
public class TemplateCallTest {

    public static class Person {
        String name;

        private Person(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }
    }

    @Test
    public void testIncludeTemplate() throws IOException {
        SimpleTemplate st = new SimpleTemplate("${", "}", EscapeMode.HTML, this.getClass(), "TemplateCallTest.txt");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("title", "TITLE");

        String rendered = st.render(model);

        // test include
        Assert.assertTrue(rendered.contains("this is for the file include test"));

        // test define
        Assert.assertTrue(rendered.contains("PARAM1: parameter-1"));
        Assert.assertTrue(rendered.contains("PARAM2: TITLE"));

        Assert.assertTrue(rendered.contains("PARAM1: TITLE"));
        Assert.assertTrue(rendered.contains("PARAM2: parameter-2"));
    }

}
