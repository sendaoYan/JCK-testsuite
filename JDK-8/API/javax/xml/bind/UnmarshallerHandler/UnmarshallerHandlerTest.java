/*
 * @test
 * @summary javax.xml.bind.UnmarshallerHandler conformance: SAX bridge surface and
 *          real parse->getResult closure through an XMLReader.
 * @run main UnmarshallerHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.UnmarshallerHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import org.xml.sax.InputSource;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;


public class UnmarshallerHandlerTest {
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

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.UnmarshallerHandler");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "org.xml.sax.ContentHandler"), "implements org.xml.sax.ContentHandler");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getResult"), "has method getResult");
        check(c.getDeclaredMethods().length == 1, "only getResult declared beyond ContentHandler");
        // --- appended deep coverage ------------------------------------------
        // real SAX closure: parse SAX events through the handler, then read the object
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        Unmarshaller u = ctx.createUnmarshaller();
        UnmarshallerHandler uh = u.getUnmarshallerHandler();
        check(uh != null, "getUnmarshallerHandler non-null");
        check(uh instanceof org.xml.sax.ContentHandler, "handler is a ContentHandler");
        // getResult before any parse: measured behavior (either null or IllegalStateException)
        boolean threwOrNot = false;
        try {
            Object early = uh.getResult();
            threwOrNot = (early == null);
        } catch (IllegalStateException e) {
            threwOrNot = true;
        }
        check(threwOrNot, "getResult before parse yields null or IllegalStateException (measured)");
        // drive a real parse
        XMLReader reader = XMLReaderFactory.createXMLReader();
        reader.setContentHandler(uh);
        String xml = "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person><name>alice</name><age>30</age></person>";
        java.io.StringReader sr = new java.io.StringReader(xml);
        reader.parse(new InputSource(sr));
        Object result = uh.getResult();
        check(result instanceof Person, "getResult yields the mapped Person, got " + result);
        Person p = (Person) result;
        check("alice".equals(p.name), "SAX channel restores name");
        check(p.age == 30, "SAX channel restores age");
        // the same unmarshaller yields a fresh handler each call
        UnmarshallerHandler uh2 = u.getUnmarshallerHandler();
        check(uh2 != null && uh2 != uh, "getUnmarshallerHandler returns a fresh handler per call");
        // getResult can be called repeatedly after parse (same object identity measured)
        Object again = uh.getResult();
        check(again == result || again.equals(result), "getResult stable across calls");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
