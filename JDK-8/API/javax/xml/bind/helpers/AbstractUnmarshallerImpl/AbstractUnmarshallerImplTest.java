/*
 * @test
 * @summary javax.xml.bind.helpers.AbstractUnmarshallerImpl conformance: delegation funnel, validation flags, handler defaults and unsupported operations.
 * @run main AbstractUnmarshallerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AbstractUnmarshallerImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // minimal concrete subclass: abstract unmarshal(XMLReader,InputSource) plus the two
    // interface methods the base class does NOT implement (unmarshal(Node), getUnmarshallerHandler)
    static class MyUnmarshaller extends javax.xml.bind.helpers.AbstractUnmarshallerImpl {
        int calls;
        Object ret;
        org.xml.sax.XMLReader lastReader;
        org.xml.sax.InputSource lastSource;
        javax.xml.bind.JAXBException toThrow;
        int nodeCalls;
        int handlerQueries;
        public Object unmarshal(org.xml.sax.XMLReader reader, org.xml.sax.InputSource source)
                throws javax.xml.bind.JAXBException {
            calls++;
            lastReader = reader;
            lastSource = source;
            if (toThrow != null) throw toThrow;
            return ret;
        }
        public Object unmarshal(org.w3c.dom.Node n) throws javax.xml.bind.JAXBException {
            nodeCalls++;
            return ret;
        }
        public javax.xml.bind.UnmarshallerHandler getUnmarshallerHandler() {
            handlerQueries++;
            return null;
        }
        boolean reportValidatingField() { return validating; }
        javax.xml.bind.UnmarshalException reportCreateUnmarshalException(org.xml.sax.SAXException e) {
            return createUnmarshalException(e);
        }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.helpers.AbstractUnmarshallerImpl");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.Unmarshaller"), "implements javax.xml.bind.Unmarshaller");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("unmarshal"), "has method unmarshal");
        check(names.contains("isValidating"), "has method isValidating");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("setValidating"), "has method setValidating");
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
        testInstantiation(c);
        testDelegation();
        testNullChecks();
        testValidatingAndProperties();
        testEventHandler();
        testUnsupported();
        testCreateUnmarshalException();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testInstantiation(Class<?> c) throws Exception {
        boolean inst = false;
        try { c.newInstance(); } catch (InstantiationException e) { inst = true; }
        check(inst, "cannot instantiate abstract class directly");
        MyUnmarshaller u = new MyUnmarshaller();
        check(u != null, "concrete subclass instantiates");
        check(u instanceof javax.xml.bind.Unmarshaller, "subclass is an Unmarshaller");
        check(java.lang.reflect.Modifier.isFinal(
                c.getMethod("unmarshal", org.xml.sax.InputSource.class).getModifiers()),
                "unmarshal(InputSource) is final (template method)");
        check(java.lang.reflect.Modifier.isAbstract(
                c.getDeclaredMethod("unmarshal", org.xml.sax.XMLReader.class, org.xml.sax.InputSource.class).getModifiers()),
                "declares abstract unmarshal(XMLReader,InputSource)");
    }

    // all input forms funnel into the single abstract method with a prepared XMLReader
    static void testDelegation() throws Exception {
        MyUnmarshaller u = new MyUnmarshaller();
        Object sentinel = new Object();
        u.ret = sentinel;

        Object r1 = u.unmarshal(new org.xml.sax.InputSource(new java.io.StringReader("<x/>")));
        check(r1 == sentinel, "unmarshal(InputSource) returns subclass result");
        check(u.lastReader != null, "base class supplies a non-null XMLReader (locked)");
        check(u.calls == 1, "exactly one funnel call");

        check(u.unmarshal(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<y/>"))) == sentinel,
                "unmarshal(Source:StreamSource) delegates");
        check(u.unmarshal(new javax.xml.transform.sax.SAXSource(
                new org.xml.sax.InputSource(new java.io.StringReader("<z/>")))) == sentinel,
                "unmarshal(Source:SAXSource) delegates");
        check(u.unmarshal(new java.io.StringReader("<w/>")) == sentinel, "unmarshal(Reader) delegates");
        check(u.unmarshal(new java.io.ByteArrayInputStream("<v/>".getBytes("UTF-8"))) == sentinel,
                "unmarshal(InputStream) delegates");
        check(u.calls == 5, "five funnel calls so far");

        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("aui", ".xml");
        try {
            java.nio.file.Files.write(tmp, "<u/>".getBytes("UTF-8"));
            check(u.unmarshal(tmp.toFile()) == sentinel, "unmarshal(File) delegates");
            String expectedSysId = tmp.toUri().toURL().toExternalForm();
            check(expectedSysId.equals(u.lastSource.getSystemId()), "unmarshal(File) sets file: systemId (locked)");
            check(u.unmarshal(tmp.toUri().toURL()) == sentinel, "unmarshal(URL) delegates");
            check(expectedSysId.equals(u.lastSource.getSystemId()), "unmarshal(URL) sets file: systemId");
        } finally {
            java.nio.file.Files.delete(tmp);
        }

        // a JAXBException thrown by the subclass propagates as the SAME instance
        javax.xml.bind.UnmarshalException mine = new javax.xml.bind.UnmarshalException("mine");
        u.toThrow = mine;
        boolean same = false;
        try {
            u.unmarshal(new org.xml.sax.InputSource(new java.io.StringReader("<x/>")));
        } catch (javax.xml.bind.UnmarshalException ue) {
            same = (ue == mine);
        }
        check(same, "subclass JAXBException propagates unchanged (no rewrapping)");
        u.toThrow = null;
    }

    static void testNullChecks() throws Exception {
        MyUnmarshaller u = new MyUnmarshaller();
        u.ret = new Object();
        boolean e1 = false;
        try { u.unmarshal((org.xml.sax.InputSource) null); } catch (IllegalArgumentException e) { e1 = true; }
        check(e1, "unmarshal(InputSource null) throws IAE 'source parameter must not be null' (locked)");
        boolean e2 = false;
        try { u.unmarshal((javax.xml.transform.Source) null); } catch (IllegalArgumentException e) { e2 = true; }
        check(e2, "unmarshal(Source null) throws IAE");
        boolean e3 = false;
        try { u.unmarshal((java.io.File) null); } catch (IllegalArgumentException e) { e3 = true; }
        check(e3, "unmarshal(File null) throws IAE");
        boolean e4 = false;
        try { u.unmarshal((java.io.Reader) null); } catch (IllegalArgumentException e) { e4 = true; }
        check(e4, "unmarshal(Reader null) throws IAE");
        boolean e5 = false;
        try { u.unmarshal((java.io.InputStream) null); } catch (IllegalArgumentException e) { e5 = true; }
        check(e5, "unmarshal(InputStream null) throws IAE");
    }

    // isValidating/setValidating plus always-failing property support
    static void testValidatingAndProperties() throws Exception {
        MyUnmarshaller u = new MyUnmarshaller();
        check(u.isValidating() == false, "isValidating default false");
        check(u.reportValidatingField() == false, "protected validating field default false");
        u.setValidating(true);
        check(u.isValidating() == true, "setValidating(true) round trip");
        check(u.reportValidatingField() == true, "setValidating updates protected field (locked)");
        u.setValidating(false);
        check(u.isValidating() == false, "setValidating(false) round trip");

        boolean pe1 = false;
        try { u.setProperty("k", "v"); } catch (javax.xml.bind.PropertyException e) { pe1 = true; }
        check(pe1, "setProperty always throws PropertyException (default contract)");
        boolean pe2 = false;
        try { u.getProperty("k"); } catch (javax.xml.bind.PropertyException e) { pe2 = true; }
        check(pe2, "getProperty always throws PropertyException");
    }

    static void testEventHandler() throws Exception {
        MyUnmarshaller u = new MyUnmarshaller();
        javax.xml.bind.ValidationEventHandler def = u.getEventHandler();
        check(def != null, "default event handler non-null");
        check(def instanceof javax.xml.bind.helpers.DefaultValidationEventHandler, "default handler is DefaultValidationEventHandler");
        javax.xml.bind.helpers.DefaultValidationEventHandler mine = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        u.setEventHandler(mine);
        check(u.getEventHandler() == mine, "setEventHandler/getEventHandler round trip");
        u.setEventHandler(null);
        check(u.getEventHandler() instanceof javax.xml.bind.helpers.DefaultValidationEventHandler,
                "setEventHandler(null) falls back to DefaultValidationEventHandler (locked)");
    }

    interface Op { void run() throws Exception; }

    static boolean throwsUOE(Op op) {
        try {
            op.run();
            return false;
        } catch (UnsupportedOperationException e) {
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    // JAXB 2.0 additions are not implemented by this helper: all throw UOE
    static void testUnsupported() throws Exception {
        final MyUnmarshaller u = new MyUnmarshaller();
        check(throwsUOE(new Op() { public void run() throws Exception { u.getSchema(); } }),
                "getSchema throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.setSchema(null); } }),
                "setSchema throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.getAttachmentUnmarshaller(); } }),
                "getAttachmentUnmarshaller throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.setAttachmentUnmarshaller(null); } }),
                "setAttachmentUnmarshaller throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.getListener(); } }),
                "getListener throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.setListener(null); } }),
                "setListener throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.setAdapter(new DummyAdapter()); } }),
                "setAdapter throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.getAdapter(DummyAdapter.class); } }),
                "getAdapter throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.unmarshal((javax.xml.stream.XMLEventReader) null); } }),
                "unmarshal(XMLEventReader) throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.unmarshal((javax.xml.stream.XMLStreamReader) null); } }),
                "unmarshal(XMLStreamReader) throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.unmarshal((org.w3c.dom.Node) null, String.class); } }),
                "unmarshal(Node,Class) throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { u.unmarshal(new javax.xml.transform.stream.StreamSource(new java.io.StringReader("<x/>")), String.class); } }),
                "unmarshal(Source,Class) throws UnsupportedOperationException");
    }

    static class DummyAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, String> {
        public String unmarshal(String v) { return v; }
        public String marshal(String v) { return v; }
    }

    // protected helper: SAX failures are wrapped into UnmarshalException with the cause linked
    static void testCreateUnmarshalException() throws Exception {
        MyUnmarshaller u = new MyUnmarshaller();
        org.xml.sax.SAXParseException spe = new org.xml.sax.SAXParseException("bad", "pub", "sys", 3, 7);
        javax.xml.bind.UnmarshalException ue = u.reportCreateUnmarshalException(spe);
        check(ue != null, "createUnmarshalException returns UnmarshalException");
        check(ue.getLinkedException() == spe, "linked exception is the SAXParseException");
        check(ue.getCause() == spe, "cause mirrors linked exception");
        org.xml.sax.SAXException se = new org.xml.sax.SAXException("sax");
        javax.xml.bind.UnmarshalException ue2 = u.reportCreateUnmarshalException(se);
        check(ue2.getLinkedException() == se, "createUnmarshalException(SAXException) links the cause");
    }
}
