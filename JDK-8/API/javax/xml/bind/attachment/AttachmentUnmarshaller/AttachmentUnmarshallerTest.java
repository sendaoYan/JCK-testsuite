/*
 * @test
 * @summary javax.xml.bind.attachment.AttachmentUnmarshaller conformance: abstract surface, default isXOPPackage and real Unmarshaller injection.
 * @run main AttachmentUnmarshallerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttachmentUnmarshallerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @javax.xml.bind.annotation.XmlRootElement
    @javax.xml.bind.annotation.XmlAccessorType(javax.xml.bind.annotation.XmlAccessType.FIELD)
    static class Person {
        @javax.xml.bind.annotation.XmlElement String name;
        @javax.xml.bind.annotation.XmlElement int age;
    }

    // recording subclass: serves fixed bytes / DataHandler per content-id
    static class RecordingUnmarshaller extends javax.xml.bind.attachment.AttachmentUnmarshaller {
        int xopQueries;
        java.util.List<String> log = new java.util.ArrayList<String>();
        public javax.activation.DataHandler getAttachmentAsDataHandler(String contentId) {
            log.add("dh:" + contentId);
            return "cid:handler".equals(contentId)
                    ? new javax.activation.DataHandler("text/plain", "served")
                    : null;
        }
        public byte[] getAttachmentAsByteArray(String contentId) {
            log.add("bytes:" + contentId);
            return new byte[]{65, 66, 67};
        }
        public boolean isXOPPackage() {
            xopQueries++;
            return super.isXOPPackage();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.attachment.AttachmentUnmarshaller");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("getAttachmentAsDataHandler"), "has method getAttachmentAsDataHandler");
        check(names.contains("getAttachmentAsByteArray"), "has method getAttachmentAsByteArray");
        check(names.contains("isXOPPackage"), "has method isXOPPackage");
        testAbstractSurface(c);
        testDefaultBehaviour();
        testCallbackSemantics();
        testRealInjection();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testAbstractSurface(Class<?> c) throws Exception {
        boolean inst = false;
        try { c.newInstance(); } catch (InstantiationException e) { inst = true; }
        check(inst, "cannot instantiate abstract class");
        check(Modifier.isAbstract(c.getDeclaredMethod("getAttachmentAsDataHandler", String.class).getModifiers()),
                "getAttachmentAsDataHandler abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("getAttachmentAsByteArray", String.class).getModifiers()),
                "getAttachmentAsByteArray abstract");
        check(!Modifier.isAbstract(c.getDeclaredMethod("isXOPPackage").getModifiers()),
                "isXOPPackage has a default implementation");
        check(c.getDeclaredMethods().length == 3, "3 declared methods");
        RecordingUnmarshaller ru = new RecordingUnmarshaller();
        check(ru instanceof javax.xml.bind.attachment.AttachmentUnmarshaller, "subclass instantiates");
    }

    static void testDefaultBehaviour() {
        RecordingUnmarshaller u = new RecordingUnmarshaller();
        check(u.isXOPPackage() == false, "default isXOPPackage() == false (locked)");
        check(u.xopQueries == 1, "override chain reaches super");
    }

    static void testCallbackSemantics() {
        RecordingUnmarshaller u = new RecordingUnmarshaller();
        byte[] b = u.getAttachmentAsByteArray("cid:x");
        check(b.length == 3, "getAttachmentAsByteArray returns served bytes");
        check(b[0] == 65 && b[1] == 66 && b[2] == 67, "served byte values {65,66,67}");
        javax.activation.DataHandler served = u.getAttachmentAsDataHandler("cid:handler");
        check(served != null, "getAttachmentAsDataHandler serves a DataHandler");
        javax.activation.DataHandler none = u.getAttachmentAsDataHandler("cid:none");
        check(none == null, "null DataHandler return permitted");
        check(u.log.size() == 3, "all three lookups logged");
        check(u.log.get(0).equals("bytes:cid:x"), "byte lookup content-id recorded");
        check(u.log.get(1).equals("dh:cid:handler"), "handler lookup content-id recorded");
    }

    // real injection: RI Unmarshaller accepts the handler and keeps unmarshalling intact
    static void testRealInjection() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Unmarshaller u = ctx.createUnmarshaller();
        check(u.getAttachmentUnmarshaller() == null, "default attachment unmarshaller null");
        RecordingUnmarshaller au = new RecordingUnmarshaller();
        u.setAttachmentUnmarshaller(au);
        check(u.getAttachmentUnmarshaller() == au, "set/getAttachmentUnmarshaller round trip");

        Object r = u.unmarshal(new java.io.StringReader(
                "<person><name>bob</name><age>7</age></person>"));
        check(r instanceof Person, "unmarshal completes with attachment handler installed");
        check("bob".equals(((Person) r).name), "round-trip name with handler installed");
        check(((Person) r).age == 7, "round-trip age with handler installed");
        // accounting: exactly one isXOPPackage query per unmarshal (locked)
        check(au.xopQueries == 1, "isXOPPackage queried exactly once per unmarshal (locked)");
        check(au.log.isEmpty(), "no attachment lookups for plain text content (locked)");

        u.unmarshal(new java.io.StringReader(
                "<person><name>kate</name><age>3</age></person>"));
        check(au.xopQueries == 2, "one more unmarshal adds exactly one query (locked)");
        check(au.log.isEmpty(), "still no attachment lookups");
    }
}
