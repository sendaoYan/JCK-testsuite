/*
 * @test
 * @summary javax.xml.bind.annotation.XmlTransient conformance: annotation interface and members.
 * @run main XmlTransientTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlTransientTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlTransient");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        testMeta();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlTransient");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 3, "@Target has exactly 3 elements (FIELD, METHOD, TYPE)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(ts.contains(java.lang.annotation.ElementType.TYPE), "@Target contains TYPE");
        check(!ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target does NOT contain PARAMETER");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no declared members");
        check(c == javax.xml.bind.annotation.XmlTransient.class,
                "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "keepHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class SecretHost {
        @javax.xml.bind.annotation.XmlTransient
        String hidden;
        @javax.xml.bind.annotation.XmlElement
        String shown;
    }

    @javax.xml.bind.annotation.XmlTransient
    static class WholeClass {
        String any;
    }

    static class NotTransient {
        @javax.xml.bind.annotation.XmlTransient
        String t;
        String none;
    }

    @javax.xml.bind.annotation.XmlTransient
    static String annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlTransient a =
                SecretHost.class.getDeclaredField("hidden")
                        .getAnnotation(javax.xml.bind.annotation.XmlTransient.class);
        check(a != null, "@XmlTransient retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlTransient.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlTransient t = WholeClass.class.getAnnotation(javax.xml.bind.annotation.XmlTransient.class);
        check(t != null, "@XmlTransient retrievable from TYPE target");
        check(t.equals(a), "field-level and type-level marker instances are equals()");
        check(t.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        javax.xml.bind.annotation.XmlTransient m =
                XmlTransientTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlTransient.class);
        check(m != null, "@XmlTransient retrievable from METHOD target");
        check(m.equals(a), "method-level marker equals field-level marker");
        check(a.toString().contains("XmlTransient"), "toString mentions XmlTransient");
        javax.xml.bind.annotation.XmlTransient other =
                NotTransient.class.getDeclaredField("t").getAnnotation(javax.xml.bind.annotation.XmlTransient.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(NotTransient.class.getDeclaredField("none")
                .getAnnotation(javax.xml.bind.annotation.XmlTransient.class) == null,
                "unannotated field in unrelated class has no @XmlTransient");
        check(SecretHost.class.getDeclaredField("shown")
                .getAnnotation(javax.xml.bind.annotation.XmlTransient.class) == null,
                "sibling field not transient");
        javax.xml.bind.annotation.XmlTransient again =
                SecretHost.class.getDeclaredField("hidden")
                        .getAnnotation(javax.xml.bind.annotation.XmlTransient.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        SecretHost h = new SecretHost();
        h.hidden = "secret";
        h.shown = "s";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(SecretHost.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(h, w);
        String xml = w.toString();
        check(xml.contains("<keepHost"), "root element present");
        check(!xml.contains("secret"), "transient field value NOT serialized");
        check(!xml.contains("hidden"), "transient field name NOT serialized");
        check(xml.contains("<shown>s</shown>"), "non-transient sibling still serialized");
        SecretHost back = (SecretHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(
                "<?xml version=\"1.0\"?><keepHost><hidden>x</hidden><shown>s</shown></keepHost>"));
        check(back.hidden == null, "unmarshal ignores unexpected element matching transient field");
        check("s".equals(back.shown), "unmarshal still binds the non-transient field");
        javax.xml.bind.JAXBContext wholeCtx = javax.xml.bind.JAXBContext.newInstance(WholeClass.class);
        check(wholeCtx != null, "TYPE-level @XmlTransient class still accepted for context creation (RI treats it as unmapped)");
    }
}
