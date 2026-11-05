/*
 * @test
 * @summary javax.xml.bind.util.JAXBSource conformance: SAXSource bridging and real marshal->Transformer closed loop.
 * @run main JAXBSourceTest
 */

public class JAXBSourceTest {
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

    static Person newPerson() {
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        return p;
    }

    public static void main(String[] args) throws Exception {
        check(javax.xml.bind.util.JAXBSource.class.getSuperclass().getName().equals("javax.xml.transform.sax.SAXSource"), "extends SAXSource");
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        testConstruction(ctx);
        testMarshalLoop(ctx);
        testMarshallerVariant(ctx);
        testNegative(ctx);
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testConstruction(javax.xml.bind.JAXBContext ctx) throws Exception {
        javax.xml.bind.util.JAXBSource s = new javax.xml.bind.util.JAXBSource(ctx, newPerson());
        check(s != null, "new JAXBSource(ctx,obj) non-null");
        check(s instanceof javax.xml.transform.Source, "is a javax.xml.transform.Source");
        check(s.getSystemId() == null, "systemId null by default");
        s.setSystemId("src-id");
        check("src-id".equals(s.getSystemId()), "setSystemId round trip");
        check(javax.xml.bind.util.JAXBSource.class.getConstructors().length == 2, "2 public constructors");
    }

    // real closed loop: identity Transformer pulls SAX events from JAXBSource into a StreamResult
    static void testMarshalLoop(javax.xml.bind.JAXBContext ctx) throws Exception {
        javax.xml.bind.util.JAXBSource s = new javax.xml.bind.util.JAXBSource(ctx, newPerson());
        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(s, new javax.xml.transform.stream.StreamResult(sw));
        String out = sw.toString();
        check(out.startsWith("<?xml"), "transformed output starts with xml declaration (locked)");
        check(out.contains("<person>"), "output contains root element person");
        check(out.contains("<name>alice</name>"), "output contains name element (mapped element name)");
        check(out.contains("<age>30</age>"), "output contains age element");

        // the same JAXBSource can be consumed again (locked: marshal-on-demand per consumption)
        java.io.StringWriter sw2 = new java.io.StringWriter();
        t.transform(s, new javax.xml.transform.stream.StreamResult(sw2));
        check(sw2.toString().contains("<person>"), "JAXBSource consumable a second time (locked)");
    }

    static void testMarshallerVariant(javax.xml.bind.JAXBContext ctx) throws Exception {
        javax.xml.bind.Marshaller m = ctx.createMarshaller();
        javax.xml.bind.util.JAXBSource s = new javax.xml.bind.util.JAXBSource(m, newPerson());
        javax.xml.transform.Transformer t = javax.xml.transform.TransformerFactory.newInstance().newTransformer();
        java.io.StringWriter sw = new java.io.StringWriter();
        t.transform(s, new javax.xml.transform.stream.StreamResult(sw));
        check(sw.toString().contains("<person>"), "marshaller variant emits document");
        check(sw.toString().contains("<age>30</age>"), "marshaller variant field mapping");
        check(s.getSystemId() == null, "marshaller variant systemId null");
    }

    static void testNegative(javax.xml.bind.JAXBContext ctx) throws Exception {
        boolean e1 = false;
        try { new javax.xml.bind.util.JAXBSource(ctx, null); }
        catch (javax.xml.bind.JAXBException e) { e1 = true; }
        check(e1, "JAXBSource(ctx,null) throws JAXBException 'Content object can not be null' (locked)");
        boolean e2 = false;
        try { new javax.xml.bind.util.JAXBSource((javax.xml.bind.JAXBContext) null, newPerson()); }
        catch (javax.xml.bind.JAXBException e) { e2 = true; }
        check(e2, "JAXBSource(null ctx,obj) throws JAXBException (locked)");
    }
}
