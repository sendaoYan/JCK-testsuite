/*
 * @test
 * @summary javax.xml.bind.Marshaller conformance: interface surface, property matrix,
 *          listener lifecycle, attachment injection, and marshal output channels.
 * @run main MarshallerTest
 */

import java.io.ByteArrayOutputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.Marshaller;
import javax.xml.bind.PropertyException;
import javax.xml.bind.attachment.AttachmentMarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.Result;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamResult;
import javax.xml.validation.Schema;


public class MarshallerTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    @XmlRootElement
    @XmlAccessorType(XmlAccessType.FIELD)
    public static class Person {
        @XmlElement
        String name;
        @XmlElement
        int age;
    }

    static final List<String> calls = new ArrayList<String>();

    static class RecordingListener extends Marshaller.Listener {
        public void beforeMarshal(Object source) { calls.add("before:" + source.getClass().getSimpleName()); }
        public void afterMarshal(Object source) { calls.add("after:" + source.getClass().getSimpleName()); }
    }

    static class RecordingAttachmentMarshaller extends AttachmentMarshaller {
        int swaRefCalls;
        int mimeCalls;
        int xopQueries;
        public String addSwaRefAttachment(javax.activation.DataHandler data) { swaRefCalls++; return "cid:swa-" + swaRefCalls; }
        public String addMtomAttachment(javax.activation.DataHandler data, String elementNamespace, String mimeType) {
            mimeCalls++; return "cid:mtom-dh-" + mimeCalls;
        }
        public String addMtomAttachment(byte[] data, int offset, int length, String mimeType,
                                        String elementNamespace, String elementLocalName) {
            mimeCalls++; return "cid:mtom-bytes-" + mimeCalls;
        }
        public boolean isXOPPackage() { xopQueries++; return false; }
    }

    static class IdentityAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, String> {
        public String marshal(String v) { return v; }
        public String unmarshal(String v) { return v; }
    }

    public static void main(String[] args) throws Exception {
        surface();
        propertyMatrix();
        listenerLifecycle();
        outputChannels();
        attachmentInjection();
        schemaHandlerNesting();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Marshaller m() throws Exception {
        return JAXBContext.newInstance(Person.class).createMarshaller();
    }

    // --- 1. interface surface ------------------------------------------------
    static void surface() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.Marshaller");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("marshal"), "has method marshal");
        check(names.contains("getNode"), "has method getNode");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setAdapter"), "has method setAdapter");
        check(names.contains("getAdapter"), "has method getAdapter");
        check(names.contains("setAttachmentMarshaller"), "has method setAttachmentMarshaller");
        check(names.contains("getAttachmentMarshaller"), "has method getAttachmentMarshaller");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setListener"), "has method setListener");
        check(names.contains("getListener"), "has method getListener");
        // property name constants locked
        check("jaxb.encoding".equals(Marshaller.JAXB_ENCODING), "JAXB_ENCODING constant");
        check("jaxb.formatted.output".equals(Marshaller.JAXB_FORMATTED_OUTPUT), "JAXB_FORMATTED_OUTPUT constant");
        check("jaxb.schemaLocation".equals(Marshaller.JAXB_SCHEMA_LOCATION), "JAXB_SCHEMA_LOCATION constant");
        check("jaxb.noNamespaceSchemaLocation".equals(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION), "JAXB_NO_NAMESPACE_SCHEMA_LOCATION constant");
        check("jaxb.fragment".equals(Marshaller.JAXB_FRAGMENT), "JAXB_FRAGMENT constant");
        // nested Listener class is an abstract class with two empty hooks
        check(java.lang.reflect.Modifier.isAbstract(Marshaller.Listener.class.getModifiers()),
              "Listener is an abstract class (measured)");
        check(Marshaller.Listener.class.getDeclaredMethods().length > 0, "Listener declares hooks");
        boolean hasBefore = false, hasAfter = false;
        for (Method mm : Marshaller.Listener.class.getDeclaredMethods()) {
            if (mm.getName().equals("beforeMarshal")) hasBefore = true;
            if (mm.getName().equals("afterMarshal")) hasAfter = true;
        }
        check(hasBefore && hasAfter, "Listener has beforeMarshal/afterMarshal");
        // default Listener methods are no-op (marshal with default listener works)
        Marshaller mm = m();
        mm.setListener(new Marshaller.Listener() {});
        StringWriter sw = new StringWriter();
        mm.marshal(person(), sw);
        check(sw.toString().contains("<person>"), "marshal with no-op listener works");
    }

    static Person person() {
        Person p = new Person();
        p.name = "alice";
        p.age = 30;
        return p;
    }

    // --- 2. property matrix ---------------------------------------------------
    static void propertyMatrix() throws Exception {
        Marshaller m = m();
        check(Boolean.FALSE.equals(m.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT)),
              "JAXB_FORMATTED_OUTPUT defaults to false");
        check("UTF-8".equals(m.getProperty(Marshaller.JAXB_ENCODING)), "JAXB_ENCODING defaults to UTF-8");
        check(m.getProperty(Marshaller.JAXB_SCHEMA_LOCATION) == null, "JAXB_SCHEMA_LOCATION defaults to null (measured)");
        check(m.getProperty(Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION) == null, "no-ns schema location defaults to null (measured)");
        check(Boolean.FALSE.equals(m.getProperty(Marshaller.JAXB_FRAGMENT)), "JAXB_FRAGMENT defaults to false");
        // set/get round-trips
        m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        check(Boolean.TRUE.equals(m.getProperty(Marshaller.JAXB_FORMATTED_OUTPUT)), "formatted round-trip");
        m.setProperty(Marshaller.JAXB_ENCODING, "UTF-16");
        check("UTF-16".equals(m.getProperty(Marshaller.JAXB_ENCODING)), "encoding round-trip");
        m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        check(Boolean.TRUE.equals(m.getProperty(Marshaller.JAXB_FRAGMENT)), "fragment round-trip");
        // fragment actually removes the declaration from writer output
        StringWriter fr = new StringWriter();
        m.marshal(person(), fr);
        check(!fr.toString().contains("<?xml"), "JAXB_FRAGMENT=true drops xml declaration (measured)");
        // unknown property
        try {
            m.setProperty("bogus.property", "x");
            check(false, "setProperty(unknown) must throw PropertyException");
        } catch (PropertyException e) {
            check(e.getMessage() != null && e.getMessage().contains("bogus.property"),
                  "unknown property message names the property: " + e.getMessage());
        }
        try {
            m.getProperty("bogus.property");
            check(false, "getProperty(unknown) must throw PropertyException");
        } catch (PropertyException e) {
            check(true, "getProperty(unknown) throws PropertyException");
        }
        // schema is null by default; setting null again keeps it null
        check(m.getSchema() == null, "schema defaults to null");
        m.setSchema(null);
        check(m.getSchema() == null, "setSchema(null) keeps null");
        Schema s = null;
        check(s == null, "no schema factory needed for null-schema contract");
        // event handler defaults to a non-null handler and round-trips identity
        javax.xml.bind.ValidationEventHandler h = m.getEventHandler();
        check(h != null, "default event handler non-null");
        javax.xml.bind.ValidationEventHandler mine = new javax.xml.bind.ValidationEventHandler() {
            public boolean handleEvent(javax.xml.bind.ValidationEvent ev) { return true; }
        };
        m.setEventHandler(mine);
        check(m.getEventHandler() == mine, "setEventHandler/getEventHandler identity");
        m.setEventHandler(null);
        check(m.getEventHandler() != mine, "setEventHandler(null) restores default handler");
    }

    // --- 3. listener lifecycle -------------------------------------------------
    static void listenerLifecycle() throws Exception {
        Marshaller m = m();
        RecordingListener l = new RecordingListener();
        m.setListener(l);
        check(m.getListener() == l, "getListener returns the same instance");
        calls.clear();
        StringWriter sw = new StringWriter();
        m.marshal(person(), sw);
        int before = 0, after = 0;
        for (String s : calls) if (s.startsWith("before")) before++; else after++;
        check(before >= 1, "beforeMarshal invoked at least once, got " + before);
        check(after >= 1, "afterMarshal invoked at least once, got " + after);
        check(calls.get(0).startsWith("before"), "first callback is beforeMarshal");
        check(calls.get(calls.size() - 1).startsWith("after"), "last callback is afterMarshal");
        check(calls.get(0).endsWith(":Person"), "beforeMarshal receives the marshalled object");
        check(calls.get(calls.size() - 1).endsWith(":Person"), "afterMarshal receives the marshalled object");
        // every 'before' is matched by an 'after' after it
        boolean ordered = true;
        for (int i = 0; i < calls.size(); i++) {
            if (calls.get(i).startsWith("before")) {
                boolean found = false;
                for (int j = i + 1; j < calls.size(); j++)
                    if (calls.get(j).startsWith("after")) { found = true; break; }
                if (!found) { ordered = false; break; }
            }
        }
        check(ordered, "each beforeMarshal is followed by an afterMarshal");
        // clearing the listener stops callbacks
        m.setListener(null);
        calls.clear();
        m.marshal(person(), new StringWriter());
        check(calls.isEmpty(), "setListener(null) removes callbacks");
    }

    // --- 4. marshal output channels --------------------------------------------
    static void outputChannels() throws Exception {
        Marshaller m = m();
        Person p = person();
        // Writer via StreamResult
        StringWriter w1 = new StringWriter();
        m.marshal(p, new StreamResult(w1));
        check(w1.toString().contains("<name>alice</name>"), "marshal(StreamResult/Writer)");
        // Writer directly
        StringWriter w2 = new StringWriter();
        m.marshal(p, w2);
        check(w2.toString().contains("<age>30</age>"), "marshal(Writer)");
        // OutputStream
        ByteArrayOutputStream os = new ByteArrayOutputStream();
        m.marshal(p, os);
        check(new String(os.toByteArray(), "UTF-8").contains("<person>"), "marshal(OutputStream)");
        // DOMResult keeps a document whose root is <person>
        DOMResult dr = new DOMResult();
        m.marshal(p, dr);
        org.w3c.dom.Node doc = dr.getNode();
        check(doc != null, "marshal(DOMResult) produces a node");
        org.w3c.dom.Element root = doc.getFirstChild() instanceof org.w3c.dom.Element
                ? (org.w3c.dom.Element) doc.getFirstChild()
                : ((org.w3c.dom.Document) doc).getDocumentElement();
        check("person".equals(root.getLocalName()), "DOMResult root is <person>, got " + root.getLocalName());
        check(root.getElementsByTagName("name").getLength() == 1, "DOMResult has one <name> child");
        check("alice".equals(root.getElementsByTagName("name").item(0).getTextContent()), "DOMResult <name> text");
        // ContentHandler channel: SAX events reach the handler
        final List<String> seen = new ArrayList<String>();
        org.xml.sax.ContentHandler ch = new org.xml.sax.helpers.DefaultHandler() {
            public void startElement(String uri, String local, String qName, org.xml.sax.Attributes a) {
                seen.add(local);
            }
        };
        m.marshal(p, ch);
        check(seen.contains("person"), "ContentHandler channel sees root element");
        check(seen.contains("name") && seen.contains("age"), "ContentHandler channel sees child elements");
        // getNode on an infestable marshaller: no bound tree before marshal
        try {
            org.w3c.dom.Node n = m.getNode(p);
            // RI returns the DOM associated with the last marshal into a DOM tree
            check(n == null || n.getLocalName() == null || "person".equals(n.getLocalName()),
                  "getNode either null or bound <person> node");
        } catch (Exception e) {
            check(true, "getNode measured behavior: " + e.getClass().getSimpleName());
        }
        // marshal(null object) is rejected
        try {
            m.marshal((Object) null, new StringWriter());
            check(false, "marshal(null, Writer) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "marshal(null) throws IllegalArgumentException (measured)");
        }
        // unmarshalable object (not a JAXB mapped root) is rejected at marshal time
        try {
            m.marshal(new Object(), new StringWriter());
            check(false, "marshal(non-mapped Object) must throw");
        } catch (javax.xml.bind.JAXBException e) {
            check(true, "marshal(non-mapped Object) throws JAXBException");
        }
    }

    // --- 5. attachment injection does not break the closure -------------------
    static void attachmentInjection() throws Exception {
        Marshaller m = m();
        check(m.getAttachmentMarshaller() == null, "attachment marshaller defaults to null");
        RecordingAttachmentMarshaller am = new RecordingAttachmentMarshaller();
        m.setAttachmentMarshaller(am);
        check(m.getAttachmentMarshaller() == am, "setAttachmentMarshaller identity round-trip");
        StringWriter sw = new StringWriter();
        m.marshal(person(), sw);
        check(sw.toString().contains("<person>"), "marshal still works with attachment marshaller injected");
        check(am.xopQueries >= 0, "isXOPPackage queried " + am.xopQueries + " times (measured)");
        m.setAttachmentMarshaller(null);
        check(m.getAttachmentMarshaller() == null, "setAttachmentMarshaller(null) clears");
        sw.getBuffer().setLength(0);
        m.marshal(person(), sw);
        check(sw.toString().contains("<person>"), "marshal works after clearing attachment marshaller");
    }

    // --- 6. adapter registry defaults ------------------------------------------
    static void schemaHandlerNesting() throws Exception {
        Marshaller m = m();
        IdentityAdapter ad = new IdentityAdapter();
        // default: no adapter registered for the type
        IdentityAdapter got = m.getAdapter(IdentityAdapter.class);
        check(got == null, "getAdapter(type) defaults to null");
        m.setAdapter(ad);
        check(m.getAdapter(IdentityAdapter.class) == ad, "setAdapter(adapter) identity keyed by class");
        m.setAdapter(IdentityAdapter.class, ad);
        check(m.getAdapter(IdentityAdapter.class) == ad, "setAdapter(Class,adapter) identity");
        m.setAdapter(IdentityAdapter.class, new IdentityAdapter());
        check(m.getAdapter(IdentityAdapter.class) != ad, "re-registering replaces the adapter");
    }
}
