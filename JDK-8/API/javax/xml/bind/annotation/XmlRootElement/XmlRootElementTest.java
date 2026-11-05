/*
 * @test
 * @summary javax.xml.bind.annotation.XmlRootElement conformance: annotation interface and members.
 * @run main XmlRootElementTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlRootElementTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlRootElement");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("namespace"), "has method namespace");
        check(names.contains("name"), "has method name");
        testMeta();
        testDefaults();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlRootElement");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 1 && et[0] == java.lang.annotation.ElementType.TYPE, "@Target({TYPE}) exactly");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 2, "exactly 2 declared members (name, namespace)");
        check(declared.containsAll(java.util.Arrays.asList("name", "namespace")), "member name set exact");
        check(c.getDeclaredMethod("name").getReturnType() == String.class, "name() returns String");
        check(c.getDeclaredMethod("namespace").getReturnType() == String.class, "namespace() returns String");
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isAbstract(m.getModifiers()), "member " + m.getName() + " is abstract");
        }
    }

    static void testDefaults() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlRootElement");
        check("##default".equals(c.getDeclaredMethod("name").getDefaultValue()), "name() default \"##default\"");
        check("##default".equals(c.getDeclaredMethod("namespace").getDefaultValue()), "namespace() default \"##default\"");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "custom", namespace = "urn:r")
    static class NamedRoot { }

    @javax.xml.bind.annotation.XmlRootElement
    static class DerivedName { }

    static class NotRoot { public String f = "x"; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlRootElement full =
                NamedRoot.class.getAnnotation(javax.xml.bind.annotation.XmlRootElement.class);
        check(full != null, "@XmlRootElement retrievable from TYPE target");
        check("custom".equals(full.name()), "explicit name read back");
        check("urn:r".equals(full.namespace()), "explicit namespace read back");
        check(full.annotationType() == javax.xml.bind.annotation.XmlRootElement.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlRootElement bare =
                DerivedName.class.getAnnotation(javax.xml.bind.annotation.XmlRootElement.class);
        check(bare != null, "bare @XmlRootElement retrievable");
        check("##default".equals(bare.name()), "bare name is \"##default\" (derived mapping)");
        check("##default".equals(bare.namespace()), "bare namespace is \"##default\"");
        check(!bare.equals(full), "different member values give non-equal instances");
        check(NotRoot.class.getAnnotation(javax.xml.bind.annotation.XmlRootElement.class) == null,
                "unannotated class has no @XmlRootElement");
        javax.xml.bind.annotation.XmlRootElement again =
                NamedRoot.class.getAnnotation(javax.xml.bind.annotation.XmlRootElement.class);
        check(full.equals(again) && full.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
        check(full.toString().contains("name=custom"), "toString exposes member values");
        check(full.toString().contains("namespace=urn:r"), "toString exposes namespace value");
    }

    static void testMarshalLoop() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(NamedRoot.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(new NamedRoot(), w);
        String xml = w.toString();
        check(xml.contains("<custom"), "explicit name used as root element");
        check(xml.contains("xmlns=\"urn:r\""), "explicit namespace becomes default xmlns declaration");
        javax.xml.bind.JAXBContext ctx2 = javax.xml.bind.JAXBContext.newInstance(DerivedName.class);
        java.io.StringWriter w2 = new java.io.StringWriter();
        ctx2.createMarshaller().marshal(new DerivedName(), w2);
        String xml2 = w2.toString();
        check(xml2.contains("<derivedName"), "default name derived: DerivedName -> derivedName (Java-to-XML decapitalization)");
        check(!xml2.contains("xmlns="), "no xmlns declaration when namespace default");
        NamedRoot backNamed = (NamedRoot) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(backNamed != null, "unmarshal back into named root class");
        DerivedName backDerived = (DerivedName) ctx2.createUnmarshaller().unmarshal(new java.io.StringReader(xml2));
        check(backDerived != null, "unmarshal back into derived-name root class");
        try {
            javax.xml.bind.JAXBContext plain = javax.xml.bind.JAXBContext.newInstance(NotRoot.class);
            plain.createMarshaller().marshal(new NotRoot(), new java.io.StringWriter());
            check(false, "marshal of instance without @XmlRootElement must fail");
        } catch (javax.xml.bind.MarshalException e) {
            check(true, "marshal without root annotation -> MarshalException");
        }
    }

}
