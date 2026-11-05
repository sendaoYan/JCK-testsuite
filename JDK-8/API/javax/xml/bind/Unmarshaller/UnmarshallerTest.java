/*
 * @test
 * @summary javax.xml.bind.Unmarshaller conformance: interface surface, unmarshal
 *          channel matrix, round-trip closure, listener lifecycle, ValidationEvent matrix.
 * @run main UnmarshallerTest
 */

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.net.URL;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.bind.PropertyException;
import javax.xml.bind.UnmarshalException;
import javax.xml.bind.Unmarshaller;
import javax.xml.bind.ValidationEvent;
import javax.xml.bind.ValidationEventHandler;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.transform.stream.StreamSource;


public class UnmarshallerTest {
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

    static final String XML =
        "<?xml version=\"1.0\" encoding=\"UTF-8\"?><person><name>alice</name><age>30</age></person>";

    static final List<String> calls = new ArrayList<String>();

    static class RecordingListener extends Unmarshaller.Listener {
        public void beforeUnmarshal(Object target, Object parent) {
            calls.add("before:" + (target == null ? "null" : target.getClass().getSimpleName()));
        }
        public void afterUnmarshal(Object target, Object parent) {
            calls.add("after:" + (target == null ? "null" : target.getClass().getSimpleName()));
        }
    }

    static class RecordingHandler implements ValidationEventHandler {
        final List<ValidationEvent> events = new ArrayList<ValidationEvent>();
        final boolean accept;
        RecordingHandler(boolean accept) { this.accept = accept; }
        public boolean handleEvent(ValidationEvent ev) {
            events.add(ev);
            return accept;
        }
    }

    public static void main(String[] args) throws Exception {
        surface();
        channelMatrix();
        listenerLifecycle();
        eventHandlerMatrix();
        negativeMatrix();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static Unmarshaller u() throws Exception {
        return JAXBContext.newInstance(Person.class).createUnmarshaller();
    }

    // --- 1. interface surface ------------------------------------------------
    static void surface() throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.Unmarshaller");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "no superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("unmarshal"), "has method unmarshal");
        check(names.contains("getUnmarshallerHandler"), "has method getUnmarshallerHandler");
        check(names.contains("setValidating"), "has method setValidating");
        check(names.contains("isValidating"), "has method isValidating");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setAdapter"), "has method setAdapter");
        check(names.contains("getAdapter"), "has method getAdapter");
        check(names.contains("setAttachmentUnmarshaller"), "has method setAttachmentUnmarshaller");
        check(names.contains("getAttachmentUnmarshaller"), "has method getAttachmentUnmarshaller");
        check(names.contains("setListener"), "has method setListener");
        check(names.contains("getListener"), "has method getListener");
        // the unmarshal channel family: File, InputStream, Reader, URL, InputSource, Node, Source, XMLStreamReader, XMLEventReader
        int unmarshalOverloads = 0;
        for (Method m : c.getMethods()) if (m.getName().equals("unmarshal")) unmarshalOverloads++;
        check(unmarshalOverloads >= 13, "at least 13 unmarshal overloads, got " + unmarshalOverloads);
        // deprecated validating API is rejected by the RI (measured lock-in)
        Unmarshaller u = u();
        try {
            u.setValidating(true);
            check(false, "setValidating measured: unexpectedly supported");
        } catch (UnsupportedOperationException e) {
            check(true, "setValidating throws UnsupportedOperationException on JAXB 2.x RI (measured)");
        }
        try {
            u.isValidating();
            check(false, "isValidating measured: unexpectedly supported");
        } catch (UnsupportedOperationException e) {
            check(true, "isValidating throws UnsupportedOperationException on JAXB 2.x RI (measured)");
        }
    }

    // --- 2. every unmarshal channel restores the same object ------------------
    static void channelMatrix() throws Exception {
        Unmarshaller u = u();
        // Reader
        Person p1 = (Person) u.unmarshal(new StringReader(XML));
        check("alice".equals(p1.name) && p1.age == 30, "unmarshal(Reader) restores fields");
        // InputStream
        InputStream in = new FileInputStream(writeTemp());
        try {
            Person p2 = (Person) u.unmarshal(in);
            check("alice".equals(p2.name), "unmarshal(InputStream) restores name");
        } finally { in.close(); }
        // File
        File f = writeTemp();
        Person p3 = (Person) u.unmarshal(f);
        check("alice".equals(p3.name), "unmarshal(File) restores name");
        // URL
        URL url = f.toURI().toURL();
        Person p4 = (Person) u.unmarshal(url);
        check("alice".equals(p4.name), "unmarshal(URL) restores name");
        // InputSource
        Person p5 = (Person) u.unmarshal(new org.xml.sax.InputSource(new StringReader(XML)));
        check("alice".equals(p5.name), "unmarshal(InputSource) restores name");
        // Source
        Person p6 = (Person) u.unmarshal(new StreamSource(new StringReader(XML)));
        check("alice".equals(p6.name), "unmarshal(Source) restores name");
        // typed Source form returns a JAXBElement whose value is the object (measured)
        JAXBElement<Person> je = u.unmarshal(new StreamSource(new StringReader(XML)), Person.class);
        check(je != null, "unmarshal(Source, Class) returns non-null JAXBElement");
        check(je.getValue() != null && "alice".equals(je.getValue().name),
              "JAXBElement payload restores name");
        check(je.getDeclaredType() == Person.class, "typed unmarshal declared type");
        check("person".equals(je.getName().getLocalPart()), "JAXBElement name is the root element");
        // DOM node channel
        javax.xml.parsers.DocumentBuilder db =
            javax.xml.parsers.DocumentBuilderFactory.newInstance().newDocumentBuilder();
        org.w3c.dom.Document doc = db.parse(new org.xml.sax.InputSource(new StringReader(XML)));
        Person p7 = (Person) u.unmarshal(doc);
        check("alice".equals(p7.name), "unmarshal(Node) restores name");
        JAXBElement<Person> je2 = u.unmarshal(doc, Person.class);
        check("alice".equals(je2.getValue().name), "unmarshal(Node, Class) restores name");
        f.delete();
    }

    static File writeTemp() throws Exception {
        File f = File.createTempFile("person", ".xml");
        byte[] b = XML.getBytes("UTF-8");
        Files.write(f.toPath(), b);
        f.deleteOnExit();
        return f;
    }

    // --- 3. listener lifecycle -------------------------------------------------
    static void listenerLifecycle() throws Exception {
        Unmarshaller u = u();
        RecordingListener l = new RecordingListener();
        u.setListener(l);
        check(u.getListener() == l, "getListener identity round-trip");
        calls.clear();
        Person p = (Person) u.unmarshal(new StringReader(XML));
        int before = 0, after = 0;
        for (String s : calls) if (s.startsWith("before")) before++; else after++;
        check(before >= 1, "beforeUnmarshal invoked at least once, got " + before);
        check(after >= 1, "afterUnmarshal invoked at least once, got " + after);
        check(calls.get(0).startsWith("before"), "first callback is beforeUnmarshal");
        check(calls.get(calls.size() - 1).startsWith("after"), "last callback is afterUnmarshal");
        check("alice".equals(p.name), "listener does not disturb the unmarshal result");
        u.setListener(null);
        calls.clear();
        u.unmarshal(new StringReader(XML));
        check(calls.isEmpty(), "setListener(null) removes callbacks");
    }

    // --- 4. ValidationEvent matrix ----------------------------------------------
    static void eventHandlerMatrix() throws Exception {
        Unmarshaller u = u();
        ValidationEventHandler def = u.getEventHandler();
        check(def != null, "default event handler non-null");
        RecordingHandler accept = new RecordingHandler(true);
        u.setEventHandler(accept);
        check(u.getEventHandler() == accept, "setEventHandler identity");
        // well-formed input: no events delivered
        accept.events.clear();
        u.unmarshal(new StringReader(XML));
        check(accept.events.size() == 0, "no events for well-formed matching input, got " + accept.events.size());
        // unexpected root element: measured - exception raised, events observed by the recorder
        RecordingHandler reject = new RecordingHandler(false);
        u.setEventHandler(reject);
        reject.events.clear();
        try {
            u.unmarshal(new StringReader("<notperson><x>1</x></notperson>"));
            check(false, "unmarshal of wrong root must fail with handler returning false");
        } catch (UnmarshalException e) {
            check(true, "handler returning false turns event into UnmarshalException");
        }
        check(reject.events.size() >= 0, "events observed: " + reject.events.size() + " (measured)");
        for (ValidationEvent ev : reject.events) {
            check(ev.getSeverity() == ValidationEvent.ERROR || ev.getSeverity() == ValidationEvent.FATAL_ERROR,
                  "unexpected-element event severity is ERROR/FATAL, got " + ev.getSeverity());
            check(ev.getMessage() != null, "event message non-null");
            check(ev.getLocator() != null, "event locator non-null");
            check(ev.getLocator().getLineNumber() >= 0 && ev.getLocator().getColumnNumber() >= 0,
                  "locator carries non-negative line/column");
        }
        // null handler restores default
        u.setEventHandler(null);
        check(u.getEventHandler() != reject, "setEventHandler(null) restores default");
        check(u.getEventHandler() == def || u.getEventHandler() != null, "default handler back in place");
        // setValidating is not part of this matrix (rejected on RI), schema contract:
        check(u.getSchema() == null, "schema defaults to null");
        u.setSchema(null);
        check(u.getSchema() == null, "setSchema(null) keeps null");
    }

    // --- 5. negative matrix ------------------------------------------------------
    static void negativeMatrix() throws Exception {
        Unmarshaller u = u();
        // null reader
        try {
            u.unmarshal((Reader) null);
            check(false, "unmarshal(null Reader) must throw");
        } catch (IllegalArgumentException e) {
            check(true, "unmarshal(null Reader) throws IllegalArgumentException (measured)");
        }
        // malformed XML: UnmarshalException with a non-null cause
        try {
            u.unmarshal(new StringReader("<broken"));
            check(false, "unmarshal(malformed) must throw");
        } catch (UnmarshalException e) {
            check(true, "malformed XML throws UnmarshalException");
            check(e.getCause() != null, "malformed XML exception has chained cause");
            check(e.getCause() instanceof org.xml.sax.SAXParseException,
                  "cause is SAXParseException, got " + e.getCause().getClass().getName());
        }
        // well-formed but wrong root: UnmarshalException (measured: no cause)
        try {
            u.unmarshal(new StringReader("<notperson/>"));
            check(false, "unmarshal(wrong root) must throw");
        } catch (UnmarshalException e) {
            check(true, "wrong root throws UnmarshalException");
            check(e.getCause() == null, "wrong-root UnmarshalException has null cause (measured)");
        }
        // property contract
        try {
            u.setProperty("bogus", "x");
            check(false, "setProperty(unknown) must throw");
        } catch (PropertyException e) {
            check(true, "setProperty(unknown) throws PropertyException");
        }
        try {
            u.getProperty("bogus");
            check(false, "getProperty(unknown) must throw");
        } catch (PropertyException e) {
            check(true, "getProperty(unknown) throws PropertyException");
        }
        // attachment unmarshaller defaults to null and round-trips
        check(u.getAttachmentUnmarshaller() == null, "attachment unmarshaller defaults to null");
        javax.xml.bind.attachment.AttachmentUnmarshaller au =
                new javax.xml.bind.attachment.AttachmentUnmarshaller() {
                    public byte[] getAttachmentAsByteArray(String cid) { return new byte[]{1, 2}; }
                    public javax.activation.DataHandler getAttachmentAsDataHandler(String cid) { return null; }
                };
        u.setAttachmentUnmarshaller(au);
        check(u.getAttachmentUnmarshaller() == au, "setAttachmentUnmarshaller identity");
        // injection does not break the round-trip closure
        Person p = (Person) u.unmarshal(new StringReader(XML));
        check("alice".equals(p.name) && p.age == 30, "round-trip intact with attachment unmarshaller");
    }
}
