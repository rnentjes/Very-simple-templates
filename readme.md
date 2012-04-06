Very simple template engine, eg:

Template:

```text
Some text.

Hello @name@,

@if(person.remark)@
Don't forget @person.remark@!
@else@
No news at this time.
@endif@

Here is the list:
@each(list as entry)@
  @entry.name@   @entry.value@
@endeach@

Your reference: @person.company.name@
```

Java:

```java
public class SimpleTamplateExample {

    public SimpleTemplateException() {
        SimpleTample st = new SimpleTemplate("@", "template.txt");

        List<NameValueObject> list = new LinkedList<NameValueObject>();

        list.add(new NameValueObject("John", "John was second!");
        list.add(new NameValueObject("Peter", "Peter lost everything.");

        Map<String, Object> model = new HashMap<String, Object>();

        model.put("name", "Mr, Smith");
        model.put("list", list);
        model.put("person", somePersonObjectWithReferences);

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
