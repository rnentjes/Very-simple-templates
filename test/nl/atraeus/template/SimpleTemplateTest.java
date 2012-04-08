package nl.atraeus.template;

import junit.framework.Assert;
import nl.astraeus.template.SimpleTemplate;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/8/12
 * Time: 1:15 PM
 */
public class SimpleTemplateTest {

    @Test
    public void testTemplateValue() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {name}");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("name", "person name");

        Assert.assertEquals("Name: person name", st.render(model));
    }

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
    public void testTemplateValue2() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {person.name}");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("person", new Person("person name"));

        Assert.assertEquals("Name: person name", st.render(model));
    }

    @Test
    public void testTemplateIf() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {if(person)}{person.name}{endif}");

        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertEquals("Name: ", st.render(model));

        model.put("person", new Person("person name"));

        Assert.assertEquals("Name: person name", st.render(model));
    }

    @Test
    public void testTemplateIfElse() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {if(person)}{person.name}{else}<empty>{endif}");

        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertEquals("Name: <empty>", st.render(model));

        model.put("person", new Person("person name"));

        Assert.assertEquals("Name: person name", st.render(model));
    }

}
