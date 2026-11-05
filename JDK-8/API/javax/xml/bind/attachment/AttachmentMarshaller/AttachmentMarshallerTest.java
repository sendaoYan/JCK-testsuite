/*
 * @test
 * @summary javax.xml.bind.attachment.AttachmentMarshaller conformance: abstract surface, default isXOPPackage and real Marshaller injection.
 * @run main AttachmentMarshallerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AttachmentMarshallerTest {
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

    // recording subclass: logs every callback, returns fixed content-ids
    static class RecordingMarshaller extends javax.xml.bind.attachment.AttachmentMarshaller {
        int xopQueries;
        java.util.List<String> log = new java.util.ArrayList<String>();
        javax.activation.DataHandler lastDh;
        public String addMtomAttachment(javax.activation.DataHandler data, String elementNamespace, String elementLocalName) {
            lastDh = data;
            log.add("mtom-dh:" + elementNamespace + "|" + elementLocalName);
            return "cid:mtom-dh";
        }
        public String addMtomAttachment(byte[] data, int offset, int length, String mimeType,
                String elementNamespace, String elementLocalName) {
            log.add("mtom-bytes:" + offset + "|" + length + "|" + mimeType + "|" + elementNamespace + "|" + elementLocalName);
            return "cid:mtom-bytes";
        }
        public String addSwaRefAttachment(javax.activation.DataHandler data) {
            lastDh = data;
            log.add("swaref");
            return "cid:swaref";
        }
        public boolean isXOPPackage() {
            xopQueries++;
            return super.isXOPPackage();
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.attachment.AttachmentMarshaller");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("addMtomAttachment"), "has method addMtomAttachment");
        check(names.contains("isXOPPackage"), "has method isXOPPackage");
        check(names.contains("addSwaRefAttachment"), "has method addSwaRefAttachment");
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
        for (Method m : c.getDeclaredMethods()) {
            String key = m.getName() + "/" + m.getParameterTypes().length;
            if (m.getName().equals("isXOPPackage")) {
                check(!Modifier.isAbstract(m.getModifiers()), key + " has a default implementation");
            } else {
                check(Modifier.isAbstract(m.getModifiers()), key + " is abstract");
            }
        }
        check(Modifier.isAbstract(c.getDeclaredMethod("addMtomAttachment",
                javax.activation.DataHandler.class, String.class, String.class).getModifiers()),
                "addMtomAttachment(DataHandler,String,String) abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("addMtomAttachment",
                byte[].class, int.class, int.class, String.class, String.class, String.class).getModifiers()),
                "addMtomAttachment(byte[],int,int,String,String,String) abstract");
        check(Modifier.isAbstract(c.getDeclaredMethod("addSwaRefAttachment",
                javax.activation.DataHandler.class).getModifiers()),
                "addSwaRefAttachment(DataHandler) abstract");
        check(c.getDeclaredMethods().length == 4, "4 declared methods");
        RecordingMarshaller rm = new RecordingMarshaller();
        check(rm instanceof javax.xml.bind.attachment.AttachmentMarshaller, "subclass instantiates");
    }

    static void testDefaultBehaviour() {
        RecordingMarshaller m = new RecordingMarshaller();
        check(m.isXOPPackage() == false, "default isXOPPackage() == false (locked)");
        check(m.xopQueries == 1, "override chain reaches super");
    }

    static void testCallbackSemantics() throws Exception {
        RecordingMarshaller m = new RecordingMarshaller();
        javax.activation.DataHandler dh = new javax.activation.DataHandler("text/plain; charset=UTF-8", "hello");
        String swaref = m.addSwaRefAttachment(dh);
        check("cid:swaref".equals(swaref), "addSwaRefAttachment returns content-id");
        check(m.lastDh == dh, "DataHandler argument passed through unchanged");
        String cid1 = m.addMtomAttachment(dh, "urn:ns", "el");
        check("cid:mtom-dh".equals(cid1), "addMtomAttachment(DataHandler,...) returns content-id");
        String cid2 = m.addMtomAttachment(new byte[]{1, 2, 3, 4}, 1, 2,
                "application/octet-stream", "urn:ns", "el2");
        check("cid:mtom-bytes".equals(cid2), "addMtomAttachment(byte[],...) returns content-id");
        check(m.log.size() == 3, "all three callbacks recorded");
        check(m.log.get(0).equals("swaref"), "swaref logged first");
        check(m.log.get(1).equals("mtom-dh:urn:ns|el"), "namespace/localName args recorded");
        check(m.log.get(2).equals("mtom-bytes:1|2|application/octet-stream|urn:ns|el2"),
                "offset/length/mime args recorded");
        // a null return is tolerated by the contract (caller then inlines the data)
        javax.xml.bind.attachment.AttachmentMarshaller nuller =
                new javax.xml.bind.attachment.AttachmentMarshaller() {
                    public String addMtomAttachment(javax.activation.DataHandler d, String ns, String ln) { return null; }
                    public String addMtomAttachment(byte[] b, int o, int l, String mt, String ns, String ln) { return null; }
                    public String addSwaRefAttachment(javax.activation.DataHandler d) { return null; }
                };
        check(nuller.addSwaRefAttachment(dh) == null, "null content-id return permitted");
        check(nuller.isXOPPackage() == false, "fresh subclass default isXOPPackage false");
    }

    // real injection: RI Marshaller accepts the handler and keeps marshalling intact
    static void testRealInjection() throws Exception {
        javax.xml.bind.JAXBContext ctx = javax.xml.bind.JAXBContext.newInstance(Person.class);
        javax.xml.bind.Marshaller m = ctx.createMarshaller();
        check(m.getAttachmentMarshaller() == null, "default attachment marshaller null");
        RecordingMarshaller am = new RecordingMarshaller();
        m.setAttachmentMarshaller(am);
        check(m.getAttachmentMarshaller() == am, "set/getAttachmentMarshaller round trip");

        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        java.io.StringWriter sw = new java.io.StringWriter();
        m.marshal(p, sw);
        String out = sw.toString();
        check(out.contains("<person>"), "marshal completes with attachment handler installed");
        check(out.contains("<name>alice</name>"), "marshalled name intact");
        check(out.contains("<age>30</age>"), "marshalled age intact");
        check(am.xopQueries == 1, "isXOPPackage queried exactly once per marshal (locked)");
        check(am.log.isEmpty(), "no attachment callbacks for plain text content (locked)");

        // second marshal: one more query, still no callbacks
        m.marshal(p, new java.io.StringWriter());
        check(am.xopQueries == 2, "isXOPPackage queried once per marshal, cumulative 2 (locked)");
        check(am.log.isEmpty(), "still no attachment callbacks");
    }
}
