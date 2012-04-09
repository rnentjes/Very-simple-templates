package nl.atraeus.template;

import junit.framework.Assert;
import nl.astraeus.template.EscapeMode;
import nl.astraeus.template.ParseException;
import nl.astraeus.template.SimpleTemplate;
import org.junit.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/8/12
 * Time: 1:15 PM
 */
public class SimpleTemplateTest {

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
    public void testTemplateValue() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {name}");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("name", "person name");

        Assert.assertEquals("Name: person name", st.render(model));
    }

    @Test
    public void testTemplatePlainValue() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {!name}");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("name", "person <name>");

        Assert.assertEquals("Name: person <name>", st.render(model));
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

    @Test
    public void testTemplateIfNot() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {ifnot(person)}<empty>{endif}");

        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertEquals("Name: <empty>", st.render(model));

        model.put("person", new Person("person name"));

        Assert.assertEquals("Name: ", st.render(model));
    }

    @Test
    public void testTemplateIfNotElse() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', EscapeMode.HTML, "Name: {ifnot(person)}<empty>{else}{person.name}{endif}");

        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertEquals("Name: <empty>", st.render(model));

        model.put("person", new Person("person <> name"));

        Assert.assertEquals("Name: person&nbsp;&lt;&gt;&nbsp;name", st.render(model));
    }

    @Test
    public void testTemplateForEach() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Name: {foreach(persons as person)}{person.name},\n{eachlast}{person.name}\n{eachend}");

        Map<String, Object> model = new HashMap<String, Object>();

        Assert.assertEquals("Name: ", st.render(model));

        List<Person> persons = new LinkedList<Person>();

        persons.add(new Person("name1"));

        model.put("persons", persons);

        Assert.assertEquals("Name: name1\n", st.render(model));

        persons.add(new Person("name2"));

        Assert.assertEquals("Name: name1,\nname2\n", st.render(model));
    }

    @Test(expected = ParseException.class)
    public void testTemplateForEachError() throws IOException {
        SimpleTemplate st = SimpleTemplate.readTemplate('{', '}', getClass().getResourceAsStream("testtemplate1.txt"));
    }

    @Test(expected = ParseException.class)
    public void testTemplateIfError() throws IOException {
        SimpleTemplate st = SimpleTemplate.readTemplate('{', '}', getClass().getResourceAsStream("testtemplate2.txt"));
    }

    @Test
    public void testTemplateBackslashEscape() {
        SimpleTemplate st = SimpleTemplate.getTemplate('{', '}', "Escape: \\{ \\\\");

        Assert.assertEquals("Escape: { \\", st.render(new HashMap<String, Object>()));
    }

}
