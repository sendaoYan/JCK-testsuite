/*
 * @test
 * @summary javax.xml.bind.annotation.XmlMixed conformance: annotation interface and members.
 * @run main XmlMixedTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlMixedTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlMixed");
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlMixed");
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
        check(c == javax.xml.bind.annotation.XmlMixed.class, "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "mixedRoot")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class MixedRoot {
        @javax.xml.bind.annotation.XmlMixed
        java.util.List<String> content;
        String note;
    }

    static class NotMixed {
        @javax.xml.bind.annotation.XmlMixed
        java.util.List<String> c;
        String none;
    }

    @javax.xml.bind.annotation.XmlMixed
    static java.util.List<String> annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlMixed a =
                MixedRoot.class.getDeclaredField("content").getAnnotation(javax.xml.bind.annotation.XmlMixed.class);
        check(a != null, "@XmlMixed retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlMixed.class, "annotationType() identity");
        java.lang.reflect.Field f = MixedRoot.class.getDeclaredField("content");
        check(java.util.List.class.isAssignableFrom(f.getType()), "@XmlMixed holder is a List (spec expectation)");
        javax.xml.bind.annotation.XmlMixed m =
                XmlMixedTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlMixed.class);
        check(m != null, "@XmlMixed retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlMixed"), "toString mentions XmlMixed");
        javax.xml.bind.annotation.XmlMixed other =
                NotMixed.class.getDeclaredField("c").getAnnotation(javax.xml.bind.annotation.XmlMixed.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(MixedRoot.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlMixed.class) == null,
                "unannotated field has no @XmlMixed");
        check(MixedRoot.class.getAnnotation(javax.xml.bind.annotation.XmlMixed.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlMixed again =
                MixedRoot.class.getDeclaredField("content").getAnnotation(javax.xml.bind.annotation.XmlMixed.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        MixedRoot r = new MixedRoot();
        r.content = new java.util.ArrayList<String>(java.util.Arrays.asList("t1 ", " t2"));
        r.note = "n";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(MixedRoot.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(r, w);
        String xml = w.toString();
        check(xml.contains("<mixedRoot"), "root element present");
        check(xml.contains(">t1   t2<"),
                "mixed String chunks concatenated verbatim as element text (whitespace preserved)");
        check(xml.indexOf("t1   t2") < xml.indexOf("<note>"),
                "mixed text precedes the ordered element child");
        check(!xml.contains("<content>"), "mixed content is NOT wrapped in a child element");
        check(xml.contains("<note>n</note>"), "sibling element field still marshals as child");
        MixedRoot back = (MixedRoot) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.content.size() == 1, "single text node unmarshals as one String chunk");
        check("t1   t2".equals(back.content.get(0)), "text chunk round-trip exact");
        MixedRoot split = (MixedRoot) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(
                "<?xml version=\"1.0\"?><mixedRoot>lead <b>bold</b> tail<mixed/><note>n</note></mixedRoot>".replace("<mixed/>", "")));
        check(split.content.size() == 2, "text separated by a child element yields 2 String chunks");
        check("lead ".equals(split.content.get(0)) && " tail".equals(split.content.get(1)),
                "chunk boundaries at element edges, whitespace retained");
        check("n".equals(split.note), "sibling field still populated");
    }
}
