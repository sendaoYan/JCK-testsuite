/*
 * @test
 * @summary javax.xml.bind.annotation.XmlMimeType conformance: annotation interface and members.
 * @run main XmlMimeTypeTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlMimeTypeTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlMimeType");
        check(c.isInterface(), "is interface");
        check(c.isAnnotation(), "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "java.lang.annotation.Annotation"), "implements java.lang.annotation.Annotation");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("value"), "has method value");
        testMeta();
        testReadBack();
        testMarshalLoop();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testMeta() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlMimeType");
        java.lang.annotation.Retention ret = c.getAnnotation(java.lang.annotation.Retention.class);
        check(ret != null && ret.value() == java.lang.annotation.RetentionPolicy.RUNTIME, "@Retention(RUNTIME)");
        java.lang.annotation.Target tgt = c.getAnnotation(java.lang.annotation.Target.class);
        check(tgt != null, "@Target present");
        java.lang.annotation.ElementType[] et = tgt.value();
        check(et.length == 3, "@Target has exactly 3 elements (FIELD, METHOD, PARAMETER)");
        java.util.Set<java.lang.annotation.ElementType> ts =
                new java.util.HashSet<java.lang.annotation.ElementType>(java.util.Arrays.asList(et));
        check(ts.contains(java.lang.annotation.ElementType.FIELD), "@Target contains FIELD");
        check(ts.contains(java.lang.annotation.ElementType.METHOD), "@Target contains METHOD");
        check(ts.contains(java.lang.annotation.ElementType.PARAMETER), "@Target contains PARAMETER");
        check(!ts.contains(java.lang.annotation.ElementType.TYPE), "@Target does NOT contain TYPE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        java.util.Set<String> declared = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) declared.add(m.getName());
        check(declared.size() == 1, "exactly 1 declared member (value)");
        check(c.getDeclaredMethod("value").getReturnType() == String.class, "value() returns String");
        check(Modifier.isAbstract(c.getDeclaredMethod("value").getModifiers()), "value() is abstract");
        check(c.getDeclaredMethod("value").getDefaultValue() == null,
                "value() has NO default (mime type must always be given)");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "mimeHost")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class MimeHost {
        @javax.xml.bind.annotation.XmlMimeType("application/octet-stream")
        byte[] blob;
        @javax.xml.bind.annotation.XmlMimeType("image/png")
        javax.activation.DataHandler picture;
        String note;
    }

    static class OtherMime {
        @javax.xml.bind.annotation.XmlMimeType("text/plain")
        byte[] t;
    }

    @javax.xml.bind.annotation.XmlMimeType("application/json")
    static byte[] annotatedMethod() { return null; }

    static void takesParam(@javax.xml.bind.annotation.XmlMimeType("text/csv") byte[] p) { }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlMimeType a =
                MimeHost.class.getDeclaredField("blob").getAnnotation(javax.xml.bind.annotation.XmlMimeType.class);
        check(a != null, "@XmlMimeType retrievable from FIELD target");
        check("application/octet-stream".equals(a.value()), "value read back exact");
        check(a.annotationType() == javax.xml.bind.annotation.XmlMimeType.class, "annotationType() identity");
        javax.xml.bind.annotation.XmlMimeType b =
                MimeHost.class.getDeclaredField("picture").getAnnotation(javax.xml.bind.annotation.XmlMimeType.class);
        check("image/png".equals(b.value()), "second mime string read back");
        check(!a.equals(b), "different values give non-equal instances");
        check(a.hashCode() != b.hashCode(), "hashCode differs for different values");
        javax.xml.bind.annotation.XmlMimeType m =
                XmlMimeTypeTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlMimeType.class);
        check(m != null, "@XmlMimeType retrievable from METHOD target");
        check("application/json".equals(m.value()), "method-level value read back");
        boolean paramFound = false;
        String paramValue = null;
        for (java.lang.reflect.Method mm : XmlMimeTypeTest.class.getDeclaredMethods()) {
            if (mm.getName().equals("takesParam")) {
                for (java.lang.annotation.Annotation pa : mm.getParameterAnnotations()[0]) {
                    if (pa instanceof javax.xml.bind.annotation.XmlMimeType) {
                        paramFound = true;
                        paramValue = ((javax.xml.bind.annotation.XmlMimeType) pa).value();
                    }
                }
            }
        }
        check(paramFound, "@XmlMimeType observable on PARAMETER target");
        check("text/csv".equals(paramValue), "parameter-level value read back");
        check(MimeHost.class.getDeclaredField("note")
                .getAnnotation(javax.xml.bind.annotation.XmlMimeType.class) == null,
                "unannotated field has no @XmlMimeType");
        check(MimeHost.class.getAnnotation(javax.xml.bind.annotation.XmlMimeType.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        javax.xml.bind.annotation.XmlMimeType other =
                OtherMime.class.getDeclaredField("t").getAnnotation(javax.xml.bind.annotation.XmlMimeType.class);
        check(other != null && "text/plain".equals(other.value()) && !other.equals(a),
                "unrelated class instance carries its own value");
        javax.xml.bind.annotation.XmlMimeType again =
                MimeHost.class.getDeclaredField("blob").getAnnotation(javax.xml.bind.annotation.XmlMimeType.class);
        check(a.equals(again) && a.hashCode() == again.hashCode(), "repeated retrieval equals/hashCode consistent");
    }

    static void testMarshalLoop() throws Exception {
        MimeHost h = new MimeHost();
        h.blob = new byte[]{1, 2};
        h.note = "n";
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(MimeHost.class);
        java.io.StringWriter w = new java.io.StringWriter();
        ctx.createMarshaller().marshal(h, w);
        String xml = w.toString();
        check(xml.contains("<mimeHost"), "root element present");
        check(xml.contains("<blob>AQI=</blob>"), "byte[] with @XmlMimeType marshals inline as base64 without AttachmentMarshaller");
        check(!xml.contains("cid:"), "no attachment reference produced");
        check(xml.contains("<note>n</note>"), "sibling plain field marshals normally");
        MimeHost back = (MimeHost) ctx.createUnmarshaller().unmarshal(new java.io.StringReader(xml));
        check(back.blob != null && back.blob.length == 2 && back.blob[0] == 1 && back.blob[1] == 2,
                "blob bytes round-trip exact");
        check("n".equals(back.note), "note round-trip");
    }
}
