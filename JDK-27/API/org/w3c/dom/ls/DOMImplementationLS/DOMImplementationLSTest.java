/*
 * @test
 * @summary org.w3c.dom.ls.DOMImplementationLS conformance: LS factory.
 * @run main DOMImplementationLSTest
 */

public class DOMImplementationLSTest {
    static int passed;

    static void check(boolean c, String m) {
        if (!c) throw new AssertionError(m);
        passed++;
    }

    static Class<?> L(String n) throws Exception { return Class.forName(n); }

    static short sconst(String c, String f) throws Exception {
        return Class.forName(c).getField(f).getShort(null);
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        Class<?> k = L("org.w3c.dom.ls.DOMImplementationLS");
        check(k.isInterface(), "org.w3c.dom.ls.DOMImplementationLS is an interface");
        check(k.getInterfaces().length == 0, "DOMImplementationLS declares no superinterface");
        check(sconst("org.w3c.dom.ls.DOMImplementationLS", "MODE_SYNCHRONOUS") == 1, "MODE_SYNCHRONOUS == 1");
        check(sconst("org.w3c.dom.ls.DOMImplementationLS", "MODE_ASYNCHRONOUS") == 2, "MODE_ASYNCHRONOUS == 2");
        check(k.getMethod("createLSParser", short.class, String.class) != null, "createLSParser(short, String)");
        check(k.getMethod("createLSSerializer") != null, "createLSSerializer()");
        check(k.getMethod("createLSInput") != null, "createLSInput()");
        check(k.getMethod("createLSOutput") != null, "createLSOutput()");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS == 1, "MODE_SYNCHRONOUS == 1");
        check(org.w3c.dom.ls.DOMImplementationLS.MODE_ASYNCHRONOUS == 2, "MODE_ASYNCHRONOUS == 2");
        org.w3c.dom.ls.DOMImplementationLS a = new org.w3c.dom.ls.DOMImplementationLS(){
            public org.w3c.dom.ls.LSParser createLSParser(short p0, java.lang.String p1) { return null; }
            public org.w3c.dom.ls.LSSerializer createLSSerializer() { return null; }
            public org.w3c.dom.ls.LSInput createLSInput() { return null; }
            public org.w3c.dom.ls.LSOutput createLSOutput() { return null; }
        };
        check(a != null, "anon impl of DOMImplementationLS");
        check(a.createLSParser((short)2, "s") == null, "createLSParser() returns fixed default");
        check(a.createLSSerializer() == null, "createLSSerializer() returns fixed default");
        check(a.createLSInput() == null, "createLSInput() returns fixed default");
        check(a.createLSOutput() == null, "createLSOutput() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMImplementationLS contract ----
    static void deepContract() throws Exception {
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.ls.DOMImplementationLS ls = (org.w3c.dom.ls.DOMImplementationLS) d.getImplementation().getFeature("LS", "3.0");
        check(ls != null, "getFeature(LS,3.0) yields DOMImplementationLS (cast channel)");
        check(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS == 1, "MODE_SYNCHRONOUS = 1");
        check(org.w3c.dom.ls.DOMImplementationLS.MODE_ASYNCHRONOUS == 2, "MODE_ASYNCHRONOUS = 2");
        org.w3c.dom.ls.LSParser p = ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null);
        check(p != null && !p.getAsync(), "createLSParser(sync, null) yields a synchronous parser");
        check(ls.createLSSerializer() != null, "createLSSerializer non-null");
        check(ls.createLSInput() != null && ls.createLSOutput() != null, "createLSInput/createLSOutput non-null");
        check(ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, "http://www.w3.org/2001/XMLSchema") != null,
            "createLSParser with the XML Schema schema-type accepted");
        try { ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_ASYNCHRONOUS, null); check(false, "async parser must be rejected"); }
        catch (org.w3c.dom.DOMException ex) { check(ex.code == org.w3c.dom.DOMException.NOT_SUPPORTED_ERR, "createLSParser(async) -> NOT_SUPPORTED_ERR(9)"); }
        // repeated factory calls yield independent objects
        check(ls.createLSParser(org.w3c.dom.ls.DOMImplementationLS.MODE_SYNCHRONOUS, null) != p, "each createLSParser returns a fresh parser");
    }
}
