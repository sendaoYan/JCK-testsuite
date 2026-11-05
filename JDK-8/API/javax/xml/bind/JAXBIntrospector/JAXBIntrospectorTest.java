/*
 * @test
 * @summary javax.xml.bind.JAXBIntrospector conformance: abstract surface and
 *          element/value unwrapping contract through a real context.
 * @run main JAXBIntrospectorTest
 */

import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.JAXBIntrospector;
import javax.xml.bind.Marshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.namespace.QName;


public class JAXBIntrospectorTest {
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
        Class<?> c = Class.forName("javax.xml.bind.JAXBIntrospector");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("isElement"), "has method isElement");
        check(names.contains("getElementName"), "has method getElementName");
        check(names.contains("getValue"), "has method getValue");
        // getValue is a static facade; isElement/getElementName are abstract instance methods
        Method gv = c.getMethod("getValue", Object.class);
        check(Modifier.isStatic(gv.getModifiers()), "getValue(Object) is static (measured)");
        Method ie = c.getMethod("isElement", Object.class);
        check(!Modifier.isStatic(ie.getModifiers()) && Modifier.isAbstract(ie.getModifiers()),
              "isElement is an abstract instance method");
        Method en = c.getMethod("getElementName", Object.class);
        check(!Modifier.isStatic(en.getModifiers()) && Modifier.isAbstract(en.getModifiers()),
              "getElementName is an abstract instance method");
        check(java.lang.reflect.Modifier.isPublic(c.getConstructors()[0].getModifiers()), "public ctor present but class is abstract (obtain via JAXBContext)");
        // --- appended deep coverage ------------------------------------------
        unwrappingContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void unwrappingContract() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        JAXBIntrospector intro = ctx.createJAXBIntrospector();
        check(intro != null, "createJAXBIntrospector non-null");

        // JAXBElement is unwrapped by getValue; isElement true
        JAXBElement<String> je = new JAXBElement<String>(new QName("urn:t", "local"), String.class, "v");
        check(intro.isElement(je), "isElement true for JAXBElement");
        check("v".equals(intro.getValue(je)), "getValue unwraps JAXBElement payload");
        // element name contract
        QName en = intro.getElementName(je);
        check(en != null && "local".equals(en.getLocalPart()) && "urn:t".equals(en.getNamespaceURI()),
              "getElementName of JAXBElement is its QName");

        // a @XmlRootElement-annotated object counts as an element (measured); getValue is identity
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        check(intro.isElement(p), "isElement true for @XmlRootElement object (measured)");
        check(intro.getValue(p) == p, "getValue(root-annotated object) returns the same reference");
        // getElementName of a root-annotated object resolves the derived root name
        QName pn = intro.getElementName(p);
        check(pn != null && "person".equals(pn.getLocalPart()), "getElementName derives root name, got " + pn);

        // marshal through the context then introspect the round-trip product
        Marshaller m = ctx.createMarshaller();
        StringWriter sw = new StringWriter();
        m.marshal(p, sw);
        Person back = (Person) ctx.createUnmarshaller().unmarshal(new StringReader(sw.toString()));
        check(intro.isElement(back), "unmarshalled root-annotated object is an element too");
        check(intro.getValue(back) == back, "getValue identity on unmarshalled object");

        // null and non-JAXB inputs are rejected
        try {
            intro.isElement(null);
            check(false, "isElement(null) must throw");
        } catch (RuntimeException e) {
            check(true, "isElement(null) throws " + e.getClass().getSimpleName() + " (measured)");
        }
        Object nullValue = intro.getValue(null); // measured: static facade tolerates null and returns null
        check(nullValue == null, "getValue(null) returns null (measured)");
        try {
            intro.getElementName(null);
            check(false, "getElementName(null) must throw");
        } catch (RuntimeException e) {
            check(true, "getElementName(null) throws " + e.getClass().getSimpleName() + " (measured)");
        }
        // non-mapped object: measured behavior of getElementName (either null or an exception)
        Object alien = new Object();
        boolean threw = false;
        QName aq = null;
        try {
            aq = intro.getElementName(alien);
        } catch (RuntimeException e) {
            threw = true; // measured: RI throws NPE for a non-mapped object
        }
        check(threw || aq == null, "getElementName(non-mapped) either null or throws (measured: threw=" + threw + ")");
    }
}
