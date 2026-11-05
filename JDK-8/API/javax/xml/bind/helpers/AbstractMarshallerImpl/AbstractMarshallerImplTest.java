/*
 * @test
 * @summary javax.xml.bind.helpers.AbstractMarshallerImpl conformance: property defaults, convenience marshal delegation and unsupported operations.
 * @run main AbstractMarshallerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;


public class AbstractMarshallerImplTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean has(String[] a, String s) {
        for (String x : a) if (s.equals(x)) return true;
        return false;
    }

    // minimal concrete subclass: implements the only class-level marshal(Object,Result)
    static class MyMarshaller extends javax.xml.bind.helpers.AbstractMarshallerImpl {
        int calls;
        java.util.List<String> resultKinds = new java.util.ArrayList<String>();
        javax.xml.transform.Result last;
        public void marshal(Object o, javax.xml.transform.Result r) throws javax.xml.bind.JAXBException {
            calls++;
            resultKinds.add(r.getClass().getSimpleName());
            last = r;
        }
        String reportEncoding() { return getEncoding(); }
        boolean reportFormatted() { return isFormattedOutput(); }
        boolean reportFragment() { return isFragment(); }
        String reportSchemaLocation() { return getSchemaLocation(); }
        String reportNoNSSchemaLocation() { return getNoNSSchemaLocation(); }
        String reportJavaEncoding(String name) throws java.io.UnsupportedEncodingException { return getJavaEncoding(name); }
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.xml.bind.helpers.AbstractMarshallerImpl");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        String[] ifs = new String[c.getInterfaces().length];
        for (int i = 0; i < ifs.length; i++) ifs[i] = c.getInterfaces()[i].getName();
        check(has(ifs, "javax.xml.bind.Marshaller"), "implements javax.xml.bind.Marshaller");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("marshal"), "has method marshal");
        check(names.contains("getNode"), "has method getNode");
        check(names.contains("setProperty"), "has method setProperty");
        check(names.contains("getProperty"), "has method getProperty");
        check(names.contains("getEventHandler"), "has method getEventHandler");
        check(names.contains("setEventHandler"), "has method setEventHandler");
        check(names.contains("setSchema"), "has method setSchema");
        check(names.contains("getSchema"), "has method getSchema");
        check(names.contains("setAdapter"), "has method setAdapter");
        check(names.contains("getAdapter"), "has method getAdapter");
        check(names.contains("setAttachmentMarshaller"), "has method setAttachmentMarshaller");
        check(names.contains("getAttachmentMarshaller"), "has method getAttachmentMarshaller");
        check(names.contains("setListener"), "has method setListener");
        check(names.contains("getListener"), "has method getListener");
        testInstantiation(c);
        testProperties();
        testDelegation();
        testNullChecks();
        testEventHandler();
        testUnsupported();
        testProtectedAccessors();
        System.out.println("PASS (" + passed + " assertions)");
    }

    static void testInstantiation(Class<?> c) throws Exception {
        boolean inst = false;
        try { c.newInstance(); } catch (InstantiationException e) { inst = true; }
        check(inst, "cannot instantiate abstract class directly");
        MyMarshaller m = new MyMarshaller();
        check(m != null, "concrete subclass (marshal(Object,Result) only) instantiates");
        check(m instanceof javax.xml.bind.Marshaller, "subclass is a Marshaller");
        check(java.lang.reflect.Modifier.isFinal(
                c.getMethod("marshal", Object.class, java.io.OutputStream.class).getModifiers()),
                "marshal(Object,OutputStream) is final (template method)");
    }

    // base class property handling: 5 recognized names, everything else -> PropertyException
    static void testProperties() throws Exception {
        MyMarshaller m = new MyMarshaller();
        check("UTF-8".equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_ENCODING)), "JAXB_ENCODING default UTF-8");
        check(Boolean.FALSE.equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT)), "JAXB_FORMATTED_OUTPUT default false");
        check(Boolean.FALSE.equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT)), "JAXB_FRAGMENT default false");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "ISO-8859-1");
        check("ISO-8859-1".equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_ENCODING)), "JAXB_ENCODING round trip");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        check(Boolean.TRUE.equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT)), "JAXB_FORMATTED_OUTPUT round trip");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        check(Boolean.TRUE.equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT)), "JAXB_FRAGMENT round trip");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_SCHEMA_LOCATION, "http://x");
        check("http://x".equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_SCHEMA_LOCATION)), "JAXB_SCHEMA_LOCATION round trip");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://y");
        check("http://y".equals(m.getProperty(javax.xml.bind.Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION)), "JAXB_NO_NAMESPACE_SCHEMA_LOCATION round trip");

        boolean pe1 = false;
        try { m.setProperty("bogus", "v"); }
        catch (javax.xml.bind.PropertyException e) { pe1 = true; }
        check(pe1, "setProperty(unknown) throws PropertyException (default contract)");
        boolean pe2 = false;
        try { m.getProperty("bogus"); }
        catch (javax.xml.bind.PropertyException e) { pe2 = true; }
        check(pe2, "getProperty(unknown) throws PropertyException");
        boolean iae = false;
        try { m.setProperty(null, "v"); }
        catch (IllegalArgumentException e) { iae = true; }
        check(iae, "setProperty(null name) throws IllegalArgumentException (locked)");
        boolean pe3 = false;
        try { m.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, Integer.valueOf(5)); }
        catch (javax.xml.bind.PropertyException e) { pe3 = true; }
        check(pe3, "JAXB_ENCODING with non-String value throws PropertyException (locked)");
    }

    // every convenience marshal overload funnels into marshal(Object,Result) with the right Result kind
    static void testDelegation() throws Exception {
        MyMarshaller m = new MyMarshaller();
        Object obj = new Object();
        m.marshal(obj, new java.io.ByteArrayOutputStream());
        check(m.calls == 1 && m.resultKinds.get(0).equals("StreamResult"), "marshal(OutputStream) delegates via StreamResult");
        m.marshal(obj, new java.io.StringWriter());
        check(m.calls == 2 && m.resultKinds.get(1).equals("StreamResult"), "marshal(Writer) delegates via StreamResult");
        m.marshal(obj, (org.xml.sax.ContentHandler) new org.xml.sax.helpers.DefaultHandler());
        check(m.calls == 3 && m.resultKinds.get(2).equals("SAXResult"), "marshal(ContentHandler) delegates via SAXResult");
        org.w3c.dom.Document doc = javax.xml.parsers.DocumentBuilderFactory.newInstance()
                .newDocumentBuilder().newDocument();
        m.marshal(obj, (org.w3c.dom.Node) doc);
        check(m.calls == 4 && m.resultKinds.get(3).equals("DOMResult"), "marshal(Node) delegates via DOMResult");
        java.nio.file.Path tmp = java.nio.file.Files.createTempFile("ami", ".xml");
        try {
            m.marshal(obj, tmp.toFile());
            check(m.calls == 5 && m.resultKinds.get(4).equals("StreamResult"), "marshal(File) delegates via StreamResult");
        } finally {
            java.nio.file.Files.delete(tmp);
        }
        // base class marshalling tolerates subclass results transparently
        m.marshal(obj, new javax.xml.transform.stream.StreamResult(new java.io.StringWriter()));
        check(m.calls == 6, "marshal(Result) reaches subclass directly");
    }

    static void testNullChecks() throws Exception {
        MyMarshaller m = new MyMarshaller();
        boolean e1 = false;
        try { m.marshal(new Object(), (java.io.OutputStream) null); }
        catch (IllegalArgumentException e) { e1 = true; }
        check(e1, "marshal(OutputStream null) throws IAE 'os parameter must not be null' (locked)");
        boolean e2 = false;
        try { m.marshal(new Object(), (java.io.Writer) null); }
        catch (IllegalArgumentException e) { e2 = true; }
        check(e2, "marshal(Writer null) throws IAE");
        boolean e3 = false;
        try { m.marshal(null, new java.io.ByteArrayOutputStream()); }
        catch (IllegalArgumentException e) { e3 = true; }
        check(e3, "marshal(null object) throws IAE 'obj parameter must not be null' (locked)");
        boolean e4 = false;
        try { m.marshal(new Object(), (org.w3c.dom.Node) null); }
        catch (IllegalArgumentException e) { e4 = true; }
        check(e4, "marshal(Node null) throws IAE");
        boolean e5 = false;
        try { m.marshal(new Object(), (org.xml.sax.ContentHandler) null); }
        catch (IllegalArgumentException e) { e5 = true; }
        check(e5, "marshal(ContentHandler null) throws IAE");
    }

    // default event handler is the shared DefaultValidationEventHandler; null restores it
    static void testEventHandler() throws Exception {
        MyMarshaller m = new MyMarshaller();
        javax.xml.bind.ValidationEventHandler def = m.getEventHandler();
        check(def != null, "default event handler non-null");
        check(def instanceof javax.xml.bind.helpers.DefaultValidationEventHandler, "default handler is DefaultValidationEventHandler");
        javax.xml.bind.helpers.DefaultValidationEventHandler mine = new javax.xml.bind.helpers.DefaultValidationEventHandler();
        m.setEventHandler(mine);
        check(m.getEventHandler() == mine, "setEventHandler/getEventHandler round trip");
        m.setEventHandler(null);
        check(m.getEventHandler() != null
                && m.getEventHandler() instanceof javax.xml.bind.helpers.DefaultValidationEventHandler,
                "setEventHandler(null) falls back to DefaultValidationEventHandler (locked)");
        check(m.getEventHandler() != mine, "null handler installs a fresh instance");
    }

    // JDK-8 RI leaves JAXB 2.0 additions unsupported in this helper: all throw UOE
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

    static void testUnsupported() throws Exception {
        final MyMarshaller m = new MyMarshaller();
        check(throwsUOE(new Op() { public void run() throws Exception { m.getSchema(); } }),
                "getSchema throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.setSchema(null); } }),
                "setSchema throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.getListener(); } }),
                "getListener throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.setListener(null); } }),
                "setListener throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.getAttachmentMarshaller(); } }),
                "getAttachmentMarshaller throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.setAttachmentMarshaller(null); } }),
                "setAttachmentMarshaller throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.getAdapter(DummyAdapter.class); } }),
                "getAdapter throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.setAdapter(new DummyAdapter()); } }),
                "setAdapter throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.getNode(new Object()); } }),
                "getNode throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.marshal(new Object(), (javax.xml.stream.XMLEventWriter) null); } }),
                "marshal(XMLEventWriter) throws UnsupportedOperationException");
        check(throwsUOE(new Op() { public void run() throws Exception { m.marshal(new Object(), (javax.xml.stream.XMLStreamWriter) null); } }),
                "marshal(XMLStreamWriter) throws UnsupportedOperationException");
    }

    static class DummyAdapter extends javax.xml.bind.annotation.adapters.XmlAdapter<String, String> {
        public String unmarshal(String v) { return v; }
        public String marshal(String v) { return v; }
    }

    // protected state accessors track the JAXB_* properties
    static void testProtectedAccessors() throws Exception {
        MyMarshaller m = new MyMarshaller();
        check(m.reportEncoding().equals("UTF-8"), "protected getEncoding default UTF-8");
        check(m.reportFormatted() == false, "protected isFormattedOutput default false");
        check(m.reportFragment() == false, "protected isFragment default false");
        check(m.reportSchemaLocation() == null, "protected getSchemaLocation default null");
        check(m.reportNoNSSchemaLocation() == null, "protected getNoNSSchemaLocation default null");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_ENCODING, "UTF-16");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
        m.setProperty(javax.xml.bind.Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
        m.setProperty(javax.xml.bind.Marshaller.JAXB_SCHEMA_LOCATION, "http://s");
        m.setProperty(javax.xml.bind.Marshaller.JAXB_NO_NAMESPACE_SCHEMA_LOCATION, "http://n");
        check(m.reportEncoding().equals("UTF-16"), "protected getEncoding follows JAXB_ENCODING");
        check(m.reportFormatted() == true, "protected isFormattedOutput follows JAXB_FORMATTED_OUTPUT");
        check(m.reportFragment() == true, "protected isFragment follows JAXB_FRAGMENT");
        check("http://s".equals(m.reportSchemaLocation()), "protected getSchemaLocation follows property");
        check("http://n".equals(m.reportNoNSSchemaLocation()), "protected getNoNSSchemaLocation follows property");
        check(m.reportJavaEncoding("UTF-8").equals("UTF-8"), "protected getJavaEncoding resolves UTF-8");
        boolean uee = false;
        try { m.reportJavaEncoding("no-such-charset"); }
        catch (java.io.UnsupportedEncodingException e) { uee = true; }
        check(uee, "protected getJavaEncoding(unknown) throws UnsupportedEncodingException (locked)");
    }
}
