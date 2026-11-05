/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAnyAttribute conformance: annotation interface and members.
 * @run main XmlAnyAttributeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlAnyAttributeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAnyAttribute");
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAnyAttribute");
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
        check(c == javax.xml.bind.annotation.XmlAnyAttribute.class,
                "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "wildAttrs")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class WildAttrs {
        @javax.xml.bind.annotation.XmlAnyAttribute
        java.util.Map<javax.xml.namespace.QName, String> others;
        @javax.xml.bind.annotation.XmlAttribute(name = "known")
        String known;
        String plain;
    }

    static class NotWild {
        @javax.xml.bind.annotation.XmlAnyAttribute
        java.util.Map<javax.xml.namespace.QName, String> m;
    }

    @javax.xml.bind.annotation.XmlAnyAttribute
    static java.util.Map<javax.xml.namespace.QName, String> annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlAnyAttribute a =
                WildAttrs.class.getDeclaredField("others").getAnnotation(javax.xml.bind.annotation.XmlAnyAttribute.class);
        check(a != null, "@XmlAnyAttribute retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlAnyAttribute.class, "annotationType() identity");
        check(WildAttrs.class.getDeclaredField("known")
                .getAnnotation(javax.xml.bind.annotation.XmlAnyAttribute.class) == null,
                "@XmlAttribute field does not carry @XmlAnyAttribute");
        check(WildAttrs.class.getDeclaredField("plain")
                .getAnnotation(javax.xml.bind.annotation.XmlAnyAttribute.class) == null,
                "unannotated field has no @XmlAnyAttribute");
        javax.xml.bind.annotation.XmlAnyAttribute m =
                XmlAnyAttributeTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlAnyAttribute.class);
        check(m != null, "@XmlAnyAttribute retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlAnyAttribute"), "toString mentions XmlAnyAttribute");
        javax.xml.bind.annotation.XmlAnyAttribute again =
                NotWild.class.getDeclaredField("m").getAnnotation(javax.xml.bind.annotation.XmlAnyAttribute.class);
        check(again.equals(a), "instances from unrelated classes still equals()");
        check(WildAttrs.class.getAnnotation(javax.xml.bind.annotation.XmlAnyAttribute.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        java.lang.reflect.Field f = WildAttrs.class.getDeclaredField("others");
        check(java.util.Map.class.isAssignableFrom(f.getType()), "wildcard holder must be a Map (spec)");
        check(f.getGenericType().toString().contains("QName"),
                "key type of the wildcard map is javax.xml.namespace.QName (spec)");
    }

    static void testMarshalLoop() throws Exception {
        WildAttrs w = new WildAttrs();
        w.others = new java.util.LinkedHashMap<javax.xml.namespace.QName, String>();
        w.others.put(new javax.xml.namespace.QName("urn:o", "ex"), "ov");
        w.others.put(new javax.xml.namespace.QName("", "flat"), "fv");
        w.known = "kv";
        w.plain = "pv";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(WildAttrs.class);
        java.io.StringWriter sw = new java.io.StringWriter();
        ctx.createMarshaller().marshal(w, sw);
        String xml = sw.toString();
        check(xml.contains("<wildAttrs"), "root element present");
        check(xml.contains("flat=\"fv\""), "no-namespace wildcard attribute rendered unqualified");
        check(xml.contains(":ex=\"ov\""), "namespaced wildcard attribute rendered with generated prefix");
        check(xml.contains("urn:o"), "namespace URI declared via xmlns for wildcard attribute");
        check(xml.contains("known=\"kv\""), "coexisting @XmlAttribute still rendered");
        check(xml.contains("<plain>pv</plain>"), "coexisting plain element field still rendered");
        WildAttrs back = (WildAttrs) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(
                "<?xml version=\"1.0\"?><wildAttrs flat=\"fv\" p:ex=\"ov\" known=\"kv\" xmlns:p=\"urn:o\"><plain>pv</plain></wildAttrs>"));
        check(back.others != null && back.others.size() == 2, "unmarshal collects every unmatched attribute into map");
        check("ov".equals(back.others.get(new javax.xml.namespace.QName("urn:o", "ex"))),
                "wildcard attribute key is (urn:o,ex) QName round-trip");
        check("fv".equals(back.others.get(new javax.xml.namespace.QName("", "flat"))),
                "no-namespace wildcard attribute keyed by empty-namespace QName");
        check("kv".equals(back.known), "explicitly mapped attribute bound to field, not the map");
        check("pv".equals(back.plain), "plain field round-trip");
    }
}
