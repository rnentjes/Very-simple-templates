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
${each(list as entry)}
  ${entry.name}   ${entry.value}
${eachlast}
-------------------------------------
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
        list.add(new NameValueObject("James", "Who is James?");

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
-------------------------------------
  James   Who is James?


Your reference: somename
```

[vst-0.5.zip](https://github.com/rnentjes/Very-simple-templates/raw/master/dist/vst-0.5.zip)

For an overview of all tags see the [wiki](https://github.com/rnentjes/Very-simple-templates/wiki)
