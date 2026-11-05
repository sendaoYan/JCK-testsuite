/*
 * @test
 * @summary javax.xml.bind.JAXB conformance: static convenience facade with
 *          marshal/unmarshal round-trip and DataBindingException contract.
 * @run main JAXBTest
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.DataBindingException;
import javax.xml.bind.JAXB;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;


public class JAXBTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Person {
        @XmlElement
        String name;
        @XmlElement
        int age;
    }

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person><name>alice</name><age>30</age></person>";

    public static void main(String[] args) throws Exception {
        check(java.lang.reflect.Modifier.isFinal(javax.xml.bind.JAXB.class.getModifiers()), "final class");
        check(java.lang.reflect.Modifier.isPublic(javax.xml.bind.JAXB.class.getModifiers())
              && javax.xml.bind.JAXB.class.getConstructors().length == 0, "public final, no public ctor (static facade)");
        Set<String> names = new HashSet<String>();
        for (java.lang.reflect.Method m : javax.xml.bind.JAXB.class.getMethods()) names.add(m.getName());
        check(names.contains("marshal"), "has marshal");
        check(names.contains("unmarshal"), "has unmarshal");
        boolean allStatic = true;
        for (java.lang.reflect.Method m : javax.xml.bind.JAXB.class.getDeclaredMethods())
            if (!Modifier.isStatic(m.getModifiers()) && !m.isSynthetic()) allStatic = false;
        check(allStatic, "all declared methods are static");
        // --- appended deep coverage -----------------------------------------
        Person p = new Person();
        p.name = "alice";
        p.age = 30;

        // marshal facade: Writer / File / OutputStream / Result
        StringWriter w = new StringWriter();
        JAXB.marshal(p, w);
        check(w.toString().contains("<person>"), "JAXB.marshal(Object, Writer) writes root element");
        check(w.toString().contains("<name>alice</name>"), "JAXB.marshal writes field element");
        check(w.toString().contains("<age>30</age>"), "JAXB.marshal writes int field element");
        File f = File.createTempFile("jaxb-facade", ".xml");
        f.deleteOnExit();
        JAXB.marshal(p, f);
        check(f.length() > 0, "JAXB.marshal(Object, File) writes bytes");
        java.io.ByteArrayOutputStream os = new java.io.ByteArrayOutputStream();
        JAXB.marshal(p, os);
        check(new String(os.toByteArray(), "UTF-8").contains("<person>"), "JAXB.marshal(Object, OutputStream)");
        StringWriter res = new StringWriter();
        JAXB.marshal(p, new StreamResult(res));
        check(res.toString().contains("<person>"), "JAXB.marshal(Object, Result)");

        // unmarshal facade: symmetric channels
        Person r1 = JAXB.unmarshal(new StringReader(XML), Person.class);
        check("alice".equals(r1.name) && r1.age == 30, "JAXB.unmarshal(Reader, Class) round-trip");
        Person r2 = JAXB.unmarshal(new StreamSource(new StringReader(XML)), Person.class);
        check("alice".equals(r2.name), "JAXB.unmarshal(Source, Class)");
        Person r3 = JAXB.unmarshal(new FileInputStream(f), Person.class);
        check("alice".equals(r3.name), "JAXB.unmarshal(InputStream, Class)");
        Person r4 = JAXB.unmarshal(new FileReader(f), Person.class);
        check("alice".equals(r4.name), "JAXB.unmarshal(Reader-from-file, Class)");
        Person r5 = JAXB.unmarshal(f, Person.class);
        check("alice".equals(r5.name), "JAXB.unmarshal(File, Class)");
        Person r6 = JAXB.unmarshal(f.toURI().toURL(), Person.class);
        check("alice".equals(r6.name), "JAXB.unmarshal(URL, Class)");
        // full closure: marshal then unmarshal through the facade only
        StringWriter again = new StringWriter();
        JAXB.marshal(r1, again);
        Person r7 = JAXB.unmarshal(new StringReader(again.toString()), Person.class);
        check("alice".equals(r7.name) && r7.age == 30, "facade marshal->unmarshal closure restores fields");

        // negative contract: failures surface as DataBindingException (unchecked)
        try {
            JAXB.unmarshal(new StringReader("<broken"), Person.class);
            check(false, "JAXB.unmarshal(malformed) must throw DataBindingException");
        } catch (DataBindingException e) {
            check(true, "malformed input -> DataBindingException");
            check(e.getCause() != null, "DataBindingException chains the underlying cause");
        }
        // measured: typed unmarshal is root-name agnostic - a mismatching root still yields the declared type
        Person rr = JAXB.unmarshal(new StringReader("<notperson/>"), Person.class);
        check(rr != null && rr.name == null && rr.age == 0,
              "typed unmarshal tolerates wrong root (measured), got defaults");
        try {
            JAXB.marshal(new Object(), new StringWriter());
            check(false, "JAXB.marshal(non-mapped) must throw");
        } catch (ClassCastException e) {
            check(true, "non-mapped marshal -> ClassCastException on JDK-8 RI (measured)");
        } catch (DataBindingException e) {
            check(true, "non-mapped marshal -> DataBindingException");
        }
        try {
            JAXB.marshal(null, new StringWriter());
            check(false, "JAXB.marshal(null) must throw");
        } catch (NullPointerException e) {
            check(true, "null marshal -> NPE on JDK-8 RI (measured)");
        } catch (IllegalArgumentException e) {
            check(true, "null marshal -> IllegalArgumentException (measured)");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
