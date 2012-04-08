package nl.astraeus.example;

import nl.astraeus.template.SimpleTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * User: rnentjes
 * Date: 4/6/12
 * Time: 11:29 PM
 */
public class TestTemplate {

    public static void main(String [] args) throws IOException {
        InputStream in = TestTemplate.class.getResourceAsStream("testtemplate.html");

        long nano = System.nanoTime();

        SimpleTemplate template = SimpleTemplate.readTemplate(in);

        System.out.println("Pasing took : "+(System.nanoTime() - nano));
        in.close();

        Map<String, Object> model = new HashMap<String, Object>();
        List<String> list = new ArrayList<String>();
        list.add("test1");
        list.add("test2");
        list.add("test3");
        list.add("test4");
        list.add("test5");
        list.add("test6");
        list.add("test7");

        model.put("pipo", "Mamaloe");
        model.put("test", template);
        model.put("list", list);

        String render;

        for (int i = 0; i < 1000; i++) {
            list.add("Index "+i);

            nano = System.nanoTime();

            model.put("pipo", "Mamaloe");
            model.put("test", template);
            model.put("list", list);

            render = template.render(model);
            //System.out.println(render);
            System.out.println("Render took : "+(System.nanoTime() - nano));
        }
    }
}
