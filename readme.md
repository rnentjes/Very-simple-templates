Very simple template engine, eg:

Template:

```text
Some text.

Hello ${name},

${if(person.remark)}
Don't forget {person.remark}!
${else}
No news at this time.
${/if}

Here is the list:
${foreach(list as entry)}
  ${entry.name}   ${entry.value}
${/each}

Your reference: ${person.company.name}
```

Java:

```java
public class SimpleTemplateExample {

    public SimpleTemplateExample() {
        SimpleTemplate st = SimpleTemplate.readTemplate("${", "}", new File("template.txt"));

        List<NameValueObject> list = new LinkedList<NameValueObject>();

        list.add(new NameValueObject("John", "John was second!");
        list.add(new NameValueObject("Peter", "Peter lost everything.");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("name", "Mr, Smith");
        model.put("list", list);
        model.put("person", somePersonObjectWithReferenceToCompany);

        Sting result = st.render(model);
    }
}
```

Output:

```text
Some text.

Hello Mr. Smith,


Don't forget to send you preferences to us!


Here is the list:

  John   John was second!
  Peter   Peter lost everything.


Your reference: somename
```

For an overview of all tags see the [wiki](https://github.com/rnentjes/Very-simple-templates/wiki)
