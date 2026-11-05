/*
 * @test
 * @summary javax.xml.bind.annotation.XmlAttachmentRef conformance: annotation interface and members.
 * @run main XmlAttachmentRefTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class XmlAttachmentRefTest {
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAttachmentRef");
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
        Class<?> c = Class.forName("javax.xml.bind.annotation.XmlAttachmentRef");
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
        check(!ts.contains(java.lang.annotation.ElementType.PACKAGE), "@Target does NOT contain PACKAGE");
        check(!c.isAnnotationPresent(java.lang.annotation.Inherited.class), "not @Inherited");
        check(!c.isAnnotationPresent(java.lang.annotation.Documented.class), "not @Documented");
        check(!c.isAnnotationPresent(java.lang.annotation.Repeatable.class), "not @Repeatable");
        check(c.getDeclaredMethods().length == 0, "marker annotation: no declared members");
        check(c == javax.xml.bind.annotation.XmlAttachmentRef.class,
                "Class.forName identity with compile-time reference");
    }

    @javax.xml.bind.annotation.XmlRootElement(name = "attRoot")
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class AttRoot {
        @javax.xml.bind.annotation.XmlAttachmentRef
        @javax.xml.bind.annotation.XmlElement
        javax.activation.DataHandler att;
    }

    static class NotSwa {
        @javax.xml.bind.annotation.XmlAttachmentRef
        @javax.xml.bind.annotation.XmlElement
        javax.activation.DataHandler h;
        String plain;
    }

    @javax.xml.bind.annotation.XmlAttachmentRef
    static javax.activation.DataHandler annotatedMethod() { return null; }

    static void takesParam(@javax.xml.bind.annotation.XmlAttachmentRef javax.activation.DataHandler p) { }

    static void testReadBack() throws Exception {
        javax.xml.bind.annotation.XmlAttachmentRef a =
                AttRoot.class.getDeclaredField("att").getAnnotation(javax.xml.bind.annotation.XmlAttachmentRef.class);
        check(a != null, "@XmlAttachmentRef retrievable from FIELD target");
        check(a.annotationType() == javax.xml.bind.annotation.XmlAttachmentRef.class, "annotationType() identity");
        check(AttRoot.class.getDeclaredField("att")
                .getAnnotation(javax.xml.bind.annotation.XmlElement.class) != null,
                "field also carries co-located @XmlElement");
        javax.xml.bind.annotation.XmlAttachmentRef m =
                XmlAttachmentRefTest.class.getDeclaredMethod("annotatedMethod")
                        .getAnnotation(javax.xml.bind.annotation.XmlAttachmentRef.class);
        check(m != null, "@XmlAttachmentRef retrievable from METHOD target");
        check(m.equals(a), "marker instances from different targets are equals()");
        check(m.hashCode() == a.hashCode(), "hashCode consistent for marker instances");
        check(a.toString().contains("XmlAttachmentRef"), "toString mentions XmlAttachmentRef");
        boolean paramFound = false;
        for (java.lang.reflect.Method mm : XmlAttachmentRefTest.class.getDeclaredMethods()) {
            if (mm.getName().equals("takesParam")) {
                for (java.lang.annotation.Annotation pa : mm.getParameterAnnotations()[0]) {
                    if (pa instanceof javax.xml.bind.annotation.XmlAttachmentRef) paramFound = true;
                }
            }
        }
        check(paramFound, "@XmlAttachmentRef observable on PARAMETER target");
        check(NotSwa.class.getDeclaredField("plain")
                .getAnnotation(javax.xml.bind.annotation.XmlAttachmentRef.class) == null,
                "unannotated field has no @XmlAttachmentRef");
        javax.xml.bind.annotation.XmlAttachmentRef other =
                NotSwa.class.getDeclaredField("h").getAnnotation(javax.xml.bind.annotation.XmlAttachmentRef.class);
        check(other != null && other.equals(a), "instances from unrelated classes still equals()");
        check(other.hashCode() == a.hashCode(), "hashCode consistent across classes");
        javax.xml.bind.annotation.XmlAttachmentRef again =
                AttRoot.class.getDeclaredField("att").getAnnotation(javax.xml.bind.annotation.XmlAttachmentRef.class);
        check(a.equals(again), "repeated retrieval from same field equals()");
        check(AttRoot.class.getAnnotation(javax.xml.bind.annotation.XmlAttachmentRef.class) == null,
                "TYPE-level retrieval is null (not a legal target)");
        check(javax.activation.DataHandler.class.isAssignableFrom(
                AttRoot.class.getDeclaredField("att").getType()),
                "SwaRef-mapped property type is DataHandler (spec expectation)");
    }

    static void testMarshalLoop() throws Exception {
        AttRoot r = new AttRoot();
        r.att = new javax.activation.DataHandler("cid:part1", "application/octet-stream");
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(AttRoot.class);
        javax.xml.bind.Marshaller ma = ctx.createMarshaller();
        final java.util.List<String> asked = new java.util.ArrayList<String>();
        ma.setAttachmentMarshaller(new javax.xml.bind.attachment.AttachmentMarshaller() {
            @Override
            public String addSwaRefAttachment(javax.activation.DataHandler data) {
                asked.add("swa:" + data.getName());
                return "cid:swaref-1";
            }
            @Override
            public String addMtomAttachment(javax.activation.DataHandler d, String elementNamespace, String elementLocalName) {
                return null;
            }
            @Override
            public String addMtomAttachment(byte[] data, int offset, int length, String mimeType,
                                            String elementNamespace, String elementLocalName) {
                return null;
            }
        });
        java.io.StringWriter w = new java.io.StringWriter();
        ma.marshal(r, w);
        String xml = w.toString();
        check(asked.size() == 1,
                "marshalling @XmlAttachmentRef property calls addSwaRefAttachment exactly once with the DataHandler");
        check(xml.contains("<attRoot>"), "root element present");
        check(xml.contains("<att>cid:swaref-1</att>"),
                "SwaRef URI returned by AttachmentMarshaller rendered as element content");
        // negative: without an AttachmentMarshaller the RI cannot produce the ref
        try {
            ctx.createMarshaller().marshal(r, new java.io.StringWriter());
            check(false, "marshal without AttachmentMarshaller must fail for SwaRef property");
        } catch (javax.xml.bind.MarshalException e) {
            check(true, "marshal without AttachmentMarshaller -> MarshalException (RI)");
        }
    }
}
