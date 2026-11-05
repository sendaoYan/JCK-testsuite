/*
 * @test
 * @summary javax.xml.bind.annotation.XmlValue conformance: annotation interface and members.
 * @run main XmlValueTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlValueTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlValue");
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlValue");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 2, "@Target has exactly 2 elements (FIELD, METHOD)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(!ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target does NOT contain PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no declared members");
        check(c == javax.xml.bind.annotation.XmlValue.class, "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "valueHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class ValueHost {
        @javax.xml.bind.annotation.XmlValue
        String core;
        @javax.xml.bind.annotation.XmlAttribute(name = "id")
        int id;
    }

    static class NotValue {
        @javax.xml.bind.annotation.XmlValue
        String v;
        String none;
    }

    @javax.xml.bind.annotation.XmlValue
    static String annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlValue a =
                ValueHost.class.getDeclaredField("core").getAnnotation(javax.xml.bind.annotation.XmlValue.class);
        check(a != null, "@XmlValue retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlValue.class, "annotationType() identity");
        check(ValueHost.class.getDeclaredField("id")
                .getAnnotation(javax.xml.bind.annotation.XmlAttribute.class) != null,
                "@XmlValue class may combine @XmlAttribute members (text-and-attributes mapping)");
        javax.xml.bind.annotation.XmlValue m =
                XmlValueTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlValue.class);
        check(m != null, "@XmlValue retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlValue"), "toString mentions XmlValue");
        javax.xml.bind.annotation.XmlValue other =
                NotValue.class.getDeclaredField("v").getAnnotation(javax.xml.bind.annotation.XmlValue.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(NotValue.class.getDeclaredField("none")
                .getAnnotation(javax.xml.bind.annotation.XmlValue.class) == null,
                "unannotated field has no @XmlValue");
        check(ValueHost.class.getAnnotation(javax.xml.bind.annotation.XmlValue.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlValue again =
                ValueHost.class.getDeclaredField("core").getAnnotation(javax.xml.bind.annotation.XmlValue.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        ValueHost h = new ValueHost();
        h.core = "text";
        h.id = 9;
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(ValueHost.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(h, w);
        String xml = w.toString();
        check(xml.contains("<valueHost"), "root element present");
        check(xml.contains("id=\"9\""), "attribute member rendered on root start tag");
        check(xml.contains(">text<") || xml.contains(">text</valueHost>"),
                "@XmlValue content rendered as the element's character data (no child tag)");
        check(!xml.contains("<core>"), "@XmlValue field NOT wrapped in its own element");
        check(!xml.contains("<value>"), "no synthetic <value> wrapper either");
        ValueHost back = (ValueHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check("text".equals(back.core), "text value round-trip");
        check(back.id == 9, "attribute round-trip");
        ValueHost multi = (ValueHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(
                "<?xml version=\"1.0\"?><valueHost id=\"3\">other</valueHost>"));
        check("other".equals(multi.core) && multi.id == 3, "second document round-trip (text + attr swapped values)");
    }
}
