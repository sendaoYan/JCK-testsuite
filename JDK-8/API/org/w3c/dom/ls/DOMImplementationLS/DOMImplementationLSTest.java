/*
 * @test
 * @summary org.w3c.dom.ls.DOMImplementationLS conformance: reflection shape checks.
 * @run main DOMImplementationLSTest
 */

import java.lang.reflect.*;

public class DOMImplementationLSTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static java.util.Set<String> set(String... v) {
        return new java.util.HashSet<String>(java.util.Arrays.asList(v));
    }
    static java.util.Set<String> ifaceNames(Class<?> c) {
        java.util.Set<String> s = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static boolean declares(Class<?> c, String name) {
        for (Method m : c.getDeclaredMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.ls.DOMImplementationLS");
        check(c != null, "org.w3c.dom.ls.DOMImplementationLS loads");
        check(c.getName().equals("org.w3c.dom.ls.DOMImplementationLS"), "binary name is org.w3c.dom.ls.DOMImplementationLS");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.ls.DOMImplementationLS is public");
        check(c.isInterface(), "org.w3c.dom.ls.DOMImplementationLS is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "createLSParser"), "declares createLSParser");
        check(declares(c, "createLSSerializer"), "declares createLSSerializer");
        check(declares(c, "createLSInput"), "declares createLSInput");
        check(declares(c, "createLSOutput"), "declares createLSOutput");
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
