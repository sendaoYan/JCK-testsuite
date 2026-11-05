/*
 * @test
 * @summary javax.xml.bind.Element conformance: legacy marker interface and
 *          JAXBElement interoperability through a real context.
 * @run main ElementTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class ElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // a class that implements the legacy marker and is a mapped root
    @javax.xml.bind.annotation.XmlRootElement(name = "marked")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    public static class Marked implements javax.xml.bind.Element {
        @javax.xml.bind.annotation.XmlElement
        String payload;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.Element");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        // marker interface: declares no methods of its own
        check(c.getDeclaredMethods().length == 0, "marker interface declares no methods");
        check(c.getInterfaces().length == 0, "marker interface extends nothing");
        check(c.getDeclaredFields().length == 0, "no fields");
        check(c.getTypeParameters().length == 0, "not generic");
        // --- appended deep coverage ------------------------------------------
        // implementable marker: a static nested class can implement it and stay JAXB-mapped
        check(javax.xml.bind.Element.class.isAssignableFrom(Marked.class), "marker is implementable");
        Marked m = new Marked();
        m.payload = "x";
        javax.xml.bind.JAXBElement<Marked> je =
                new javax.xml.bind.JAXBElement<Marked>(
                        new javax.xml.namespace.QName("urn:e", "marked"), Marked.class, m);
        // measured: JAXBElement does NOT implement the legacy marker; the marker is for user beans
        check(!(je instanceof javax.xml.bind.Element), "JAXBElement is not a legacy Element (measured)");
        // marshal a JAXBElement holding a marker-implementing payload
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Marked.class);
        javax.xml.bind.Marshaller mar = ctx.createMarshaller();
        StringWriter sw = new StringWriter();
        mar.marshal(je, sw);
        check(sw.toString().contains("marked"), "marshal element uses the qname local name");
        check(sw.toString().contains("urn:e"), "marshal element honors namespace");
        // and a plain Marked instance marshals under its derived root
        StringWriter sw2 = new StringWriter();
        mar.marshal(m, sw2);
        check(sw2.toString().contains("<marked>"), "marker-implementing bean marshals under @XmlRootElement name");
        // round-trip
        Marked back = (Marked) ctx.createUnmarshaller().unmarshal(new StringReader(sw2.toString()));
        check("x".equals(back.payload), "round-trip restores marker bean payload");
        check(back instanceof javax.xml.bind.Element, "restored bean still implements the marker");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
