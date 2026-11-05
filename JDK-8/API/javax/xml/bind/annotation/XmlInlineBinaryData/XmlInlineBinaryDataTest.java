/*
 * @test
 * @summary javax.xml.bind.annotation.XmlInlineBinaryData conformance: annotation interface and members.
 * @run main XmlInlineBinaryDataTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlInlineBinaryDataTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlInlineBinaryData");
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlInlineBinaryData");
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
        check(ts.contains(java.lang.annotation.ElementType.TYPE), "@Target contains TYPE (unusual among JAXB field annotations)");
        check(!ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target does NOT contain PARAMETER");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no declared members");
        check(c == javax.xml.bind.annotation.XmlInlineBinaryData.class,
                "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "inlineBin")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class InlineBin {
        @javax.xml.bind.annotation.XmlInlineBinaryData
        byte[] blob;
        String note;
    }

    @javax.xml.bind.annotation.XmlInlineBinaryData
    static class TypeLevel {
        String none;
    }

    static class NotInline {
        @javax.xml.bind.annotation.XmlInlineBinaryData
        byte[] b;
    }

    @javax.xml.bind.annotation.XmlInlineBinaryData
    static byte[] annotatedMethod() { return null; }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlInlineBinaryData a =
                InlineBin.class.getDeclaredField("blob")
                        .getAnnotation(javax.xml.bind.annotation.XmlInlineBinaryData.class);
        check(a != null, "@XmlInlineBinaryData retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlInlineBinaryData.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlInlineBinaryData t =
                TypeLevel.class.getAnnotation(javax.xml.bind.annotation.XmlInlineBinaryData.class);
        check(t != null, "@XmlInlineBinaryData retrievable from TYPE target");
        check(t.equals(a), "field-level and type-level marker instances are equals()");
        check(t.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        javax.xml.bind.annotation.XmlInlineBinaryData m =
                XmlInlineBinaryDataTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlInlineBinaryData.class);
        check(m != null, "@XmlInlineBinaryData retrievable from METHOD target");
        check(m.equals(a), "method-level marker equals field-level marker");
        check(a.toString().contains("XmlInlineBinaryData"), "toString mentions XmlInlineBinaryData");
        javax.xml.bind.annotation.XmlInlineBinaryData other =
                NotInline.class.getDeclaredField("b").getAnnotation(javax.xml.bind.annotation.XmlInlineBinaryData.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(TypeLevel.class.getDeclaredField("none")
                .getAnnotation(javax.xml.bind.annotation.XmlInlineBinaryData.class) == null,
                "TYPE-level placement does not implicitly annotate fields");
        javax.xml.bind.annotation.XmlInlineBinaryData again =
                InlineBin.class.getDeclaredField("blob")
                        .getAnnotation(javax.xml.bind.annotation.XmlInlineBinaryData.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        InlineBin ib = new InlineBin();
        ib.blob = new byte[]{3, 4, (byte) 0xff};
        ib.note = "n";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(InlineBin.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(ib, w);
        String xml = w.toString();
        check(xml.contains("<inlineBin"), "root element present");
        check(xml.contains("<blob>AwT/</blob>"), "inline binary data marshalled as base64 text child");
        check("AwT/".equals(javax.xml.bind.DatatypeConverter.printBase64Binary(new byte[]{3, 4, (byte) 0xff})),
                "base64 literal cross-checked against DatatypeConverter");
        check(!xml.contains("cid:"), "no attachment reference (cid:) produced for inlined data");
        check(xml.contains("<note>n</note>"), "sibling plain field marshals normally");
        InlineBin back = (InlineBin) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.blob != null && back.blob.length == 3, "blob length round-trip");
        check(back.blob[0] == 3 && back.blob[1] == 4 && (back.blob[2] & 0xff) == 0xff, "blob bytes round-trip exact");
        check("n".equals(back.note), "note round-trip");
    }
}
