/*
 * @test
 * @summary javax.xml.bind.JAXBContext conformance: abstract factory hierarchy,
 *          newInstance matrices, and a real marshal/unmarshal round-trip closure.
 * @run main JAXBContextTest
 */

import java.io.File;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.SchemaOutputResolver;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;


public class JAXBContextTest {
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

    static class RecordingResolver extends SchemaOutputResolver {
        int calls;
        String lastNamespace;
        public Result createOutput(String namespaceUri, String suggestedFileName) {
            calls++;
            lastNamespace = namespaceUri;
            File f;
            try {
                f = File.createTempFile("ctx-schema", ".xsd");
            } catch (java.io.IOException e) {
                throw new RuntimeException(e);
            }
            f.deleteOnExit();
            return new StreamResult(f);
        }
    }

    public static void main(String[] args) throws Exception {
        hierarchy();
        newInstanceMatrices();
        roundTrip();
        schemaGeneration();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // --- 1. structure: abstract guarded factory -----------------------------
    static void hierarchy() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.JAXBContext");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("newInstance"), "has method newInstance");
        check(names.contains("createUnmarshaller"), "has method createUnmarshaller");
        check(names.contains("createMarshaller"), "has method createMarshaller");
        check(names.contains("createValidator"), "has method createValidator");
        check(names.contains("createBinder"), "has method createBinder");
        check(names.contains("createJAXBIntrospector"), "has method createJAXBIntrospector");
        check(names.contains("generateSchema"), "has method generateSchema");
        // all public constructors must be absent: instantiation only via newInstance
        check(c.getConstructors().length == 0, "no public constructors (guarded factory)");
        // newInstance overloads present with expected arity
        boolean hasVarargsClass = false, hasStrLoader = false, hasStrLoaderMap = false, hasClassArrMap = false;
        for (Method m : c.getMethods()) {
            if (!m.getName().equals("newInstance")) continue;
            Class<?>[] ps = m.getParameterTypes();
            if (ps.length == 1 && ps[0] == Class[].class) hasVarargsClass = true;
            if (ps.length == 2 && ps[0] == String.class && ps[1] == ClassLoader.class) hasStrLoader = true;
            if (ps.length == 3 && ps[0] == String.class && ps[1] == ClassLoader.class) hasStrLoaderMap = true;
            if (ps.length == 2 && ps[0] == Class[].class && ps[1] == java.util.Map.class) hasClassArrMap = true;
        }
        check(hasVarargsClass, "newInstance(Class...) present");
        check(hasStrLoader, "newInstance(String, ClassLoader) present");
        check(hasStrLoaderMap, "newInstance(String, ClassLoader, Map) present");
        check(hasClassArrMap, "newInstance(Class[], Map) present");
    }

    // --- 2. newInstance matrices: legal and negative ------------------------
    static void newInstanceMatrices() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        check(ctx != null, "newInstance(Class) non-null");
        check(ctx.createMarshaller() != null, "createMarshaller non-null");
        check(ctx.createUnmarshaller() != null, "createUnmarshaller non-null");
        check(ctx.createBinder() != null, "createBinder() non-null");
        check(ctx.createBinder(org.w3c.dom.Node.class) != null, "createBinder(Node.class) non-null");
        check(ctx.createJAXBIntrospector() != null, "createJAXBIntrospector non-null");
        // two calls of the same context factory yield independent (but working) contexts
        JAXBContext ctx2 = JAXBContext.newInstance(Person.class);
        check(ctx2 != null, "second newInstance non-null");
        check(ctx.createMarshaller() != ctx2.createMarshaller(), "marshallers are per-call objects");
        // class array + properties map form (JAXB 2.2) and classloader forms
        ClassLoader cl = Thread.currentThread().getContextClassLoader();
        if (cl == null) cl = Person.class.getClassLoader();
        JAXBContext ctx3 = JAXBContext.newInstance(new Class<?>[]{Person.class}, new java.util.HashMap<String, Object>());
        check(ctx3 != null, "newInstance(Class[], Map) non-null");
        // the deprecated createValidator: JAXB 2.x RI rejects it (measured lock-in)
        try {
            ctx.createValidator();
            check(false, "createValidator measured: unexpectedly supported");
        } catch (UnsupportedOperationException e) {
            check(true, "createValidator throws UnsupportedOperationException on JAXB 2.x RI (measured)");
            check(e.getMessage() != null, "createValidator failure has a message");
        }
        // newInstance() with no classes: measured behavior - empty context, no exception
        JAXBContext empty = null;
        try {
            empty = JAXBContext.newInstance();
            check(empty != null, "newInstance() (no args) returns non-null context (measured)");
        } catch (JAXBException e) {
            check(false, "newInstance() measured: JAXBException " + e.getMessage());
        }
        // unknown context path: JAXBException (message mentions ObjectFactory/jaxb.index)
        try {
            JAXBContext.newInstance("no.such.pkg.Cls");
            check(false, "newInstance(unknown path) must throw JAXBException");
        } catch (JAXBException e) {
            check(e.getMessage() != null && (e.getMessage().contains("ObjectFactory") || e.getMessage().contains("jaxb.index")),
                  "unknown path message mentions ObjectFactory/jaxb.index: " + e.getMessage());
        }
        // unknown class name string with loader
        try {
            JAXBContext.newInstance("no.such.Svc", cl);
            check(false, "newInstance(unknown String, loader) must throw");
        } catch (JAXBException e) {
            check(true, "newInstance(unknown String, loader) throws JAXBException");
        }
        // null Class array element
        try {
            JAXBContext.newInstance(new Class<?>[]{null});
            check(false, "newInstance((Class)null element) must throw");
        } catch (JAXBException e) {
            check(true, "newInstance(null element) throws JAXBException");
        } catch (IllegalArgumentException e) {
            check(true, "newInstance(null element) throws IllegalArgumentException (measured)");
        }
    }

    // --- 3. the element-name mapping and round-trip closure -----------------
    static void roundTrip() throws Exception {
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        Marshaller m = ctx.createMarshaller();
        StringWriter sw = new StringWriter();
        m.marshal(p, sw);
        String xml = sw.toString();
        check(xml.contains("<?xml"), "output has xml declaration");
        check(xml.contains("<person>"), "root element name defaults to decapitalized simple name");
        check(xml.contains("<name>alice</name>"), "field name maps to <name> element");
        check(xml.contains("<age>30</age>"), "int field maps to <age> element");
        check(xml.indexOf("<person>") < xml.indexOf("<name>alice</name>"), "root opens before children");
        Unmarshaller u = ctx.createUnmarshaller();
        Person back = (Person) u.unmarshal(new StringReader(xml));
        check("alice".equals(back.name), "round-trip restores name");
        check(back.age == 30, "round-trip restores age");
        // default property contract seen through the context-built marshaller
        check(Boolean.FALSE.equals(m.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT)),
              "JAXB_FORMATTED_OUTPUT default is false");
        // JAXB_ENCODING property round-trips through marshaller
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
        check("UTF-8".equals(m.getProperty(Marshaller.JAXB_ENCODING)), "JAXB_ENCODING set/get round-trip");
        // formatted output changes bytes
        StringWriter fmt = new StringWriter();
        Marshaller m2 = ctx.createMarshaller();
        m2.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m2.marshal(p, fmt);
        check(fmt.toString().contains("\n"), "formatted output contains newline");
        check(fmt.toString().contains("<person"), "formatted output still has root element");
        // UTF-16 encoding request shows up in the declaration
        StringWriter u16 = new StringWriter();
        Marshaller m3 = ctx.createMarshaller();
        m3.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
        m3.marshal(p, u16);
        check(u16.toString().contains("UTF-16"), "JAXB_ENCODING=UTF-16 appears in declaration");
        // no-ns schema location properties round-trip
        m3.setProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "p.xsd");
        check("p.xsd".equals(m3.getProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION)),
              "schema location property round-trip");
        m3.setProperty(Marshaller.JAXB_SCHEMA_LOCATION, "urn:x a.xsd");
        check("urn:x a.xsd".equals(m3.getProperty(Marshaller.JAXB_SCHEMA_LOCATION)),
              "JAXB_SCHEMA_LOCATION round-trip");
    }

    // --- 4. generateSchema drives the SchemaOutputResolver ------------------
    static void schemaGeneration() throws Exception {
        JAXBContext ctx = JAXBContext.newInstance(Person.class);
        RecordingResolver r = new RecordingResolver();
        ctx.generateSchema(r);
        check(r.calls == 1, "generateSchema invokes resolver exactly once (measured), got " + r.calls);
        check("".equals(r.lastNamespace) || r.lastNamespace == null,
              "no-namespace classes resolve with empty namespace, got " + r.lastNamespace);
    }
}
