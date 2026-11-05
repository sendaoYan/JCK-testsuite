/*
 * @test
 * @summary javax.xml.bind.util.JAXBResult conformance: SAXResult bridging and real Transformer->unmarshal closed loop.
 * @run main JAXBResultTest
 */

public class JAXBResultTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlElement String name;
        @javax.xml.bind.annotation.XmlElement int age;
    }

    static final String XML = "<person><name>alice</name><age>30</age></person>";

    public static void main(String[] args) throws Exception {
        check(javax.xml.bind.util.JAXBResult.class.getSuperclass().getName().equals("javax.xml.transform.sax.SAXResult"), "extends SAXResult");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : javax.xml.bind.util.JAXBResult.class.getMethods()) names.add(m.getName());
        check(names.contains("getResult"), "has getResult");
        testConstruction();
        testBeforeTransform();
        testTransformerLoop();
        testUnmarshallerVariant();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstruction() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.util.JAXBResult r = new javax.xml.bind.util.JAXBResult(ctx);
        check(r != null, "new JAXBResult(JAXBContext) non-null");
        check(r instanceof javax.xml.transform.Result, "is a javax.xml.transform.Result");
        check(r.getSystemId() == null, "systemId null before use");
        check(r.getSystemId() == null, "systemId default null (SAXResult contract)");
        r.setSystemId("sys-id");
        check("sys-id".equals(r.getSystemId()), "setSystemId inherited round trip");
        check(javax.xml.bind.util.JAXBResult.class.getConstructors().length == 2, "2 public constructors");
        javax.xml.bind.util.JAXBResult r2 = new javax.xml.bind.util.JAXBResult(ctx.createUnmarshaller());
        check(r2 != null, "new JAXBResult(Unmarshaller) non-null");
    }

    // getResult() before any transformation: locked IllegalStateException
    static void testBeforeTransform() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.util.JAXBResult r = new javax.xml.bind.util.JAXBResult(ctx);
        boolean ise = false;
        try {
            r.getResult();
        } catch (IllegalStateException e) {
            ise = true;
        }
        check(ise, "getResult() before transform throws IllegalStateException (locked)");
    }

    // real closed loop: identity Transformer pumps SAX events into JAXBResult, then getResult()
    static void testTransformerLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.util.JAXBResult r = new javax.xml.bind.util.JAXBResult(ctx);
        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XML)), r);
        Object res = r.getResult();
        check(res != null, "getResult() non-null after transform");
        check(res instanceof Person, "transformed result has the bound type");
        check("alice".equals(((Person) res).name), "round-trip name via Transformer");
        check(((Person) res).age == 30, "round-trip age via Transformer");
        check(r.getResult() == res, "getResult() is repeatable and stable (same instance, locked)");

        // second JAXBResult instance works independently
        javax.xml.bind.util.JAXBResult r2 = new javax.xml.bind.util.JAXBResult(ctx);
        t.transform(new javax.xml.transform.stream.StreamSource(new java.io.StringReader(XML)), r2);
        Person p2 = (Person) r2.getResult();
        check("alice".equals(p2.name) && p2.age == 30, "second instance independent loop");
    }

    // constructor taking an Unmarshaller shares the same loop
    static void testUnmarshallerVariant() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.util.JAXBResult r = new javax.xml.bind.util.JAXBResult(ctx.createUnmarshaller());
        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        t.transform(new javax.xml.transform.stream.StreamSource(
                new java.io.StringReader("<person><name>bob</name><age>9</age></person>")), r);
        Object res = r.getResult();
        check(res instanceof Person, "unmarshaller variant result type");
        check("bob".equals(((Person) res).name) && ((Person) res).age == 9, "unmarshaller variant fields equal");
    }
}
