/*
 * @test
 * @summary org.w3c.dom.NameList conformance: interface and methods.
 * @run main NameListTest
 */

import org.w3c.dom.NameList;

public class NameListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(NameList.class.isInterface(), "NameList is an interface");
        check(NameList.class.getMethod("getName", int.class)
                .getReturnType() == String.class, "getName(int) returns String");
        check(NameList.class.getMethod("getNamespaceURI", int.class)
                .getReturnType() == String.class, "getNamespaceURI(int) returns String");
        check(NameList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.NameList a = new org.w3c.dom.NameList(){
            public java.lang.String getName(int p0) { return "s"; }
            public java.lang.String getNamespaceURI(int p0) { return "s"; }
            public int getLength() { return 3; }
            public boolean contains(java.lang.String p0) { return true; }
            public boolean containsNS(java.lang.String p0, java.lang.String p1) { return true; }
        };
        check(a != null, "anon impl of NameList");
        check("s".equals(a.getName(3)), "getName() returns fixed default");
        check("s".equals(a.getNamespaceURI(3)), "getNamespaceURI() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.contains("s") == true, "contains() returns fixed default");
        check(a.containsNS("s", "s") == true, "containsNS() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral NameList contract (interface + reflection) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.NameList");
        check(c.isInterface(), "NameList is an interface");
        check(c.getMethod("getName", int.class).getReturnType() == String.class, "getName(int) returns String");
        check(c.getMethod("getNamespaceURI", int.class).getReturnType() == String.class, "getNamespaceURI(int) returns String");
        check(c.getMethod("getLength").getReturnType() == int.class, "getLength() returns int");
        check(c.getMethod("contains", String.class).getReturnType() == boolean.class, "contains(String) boolean");
        check(c.getMethod("containsNS", String.class, String.class).getReturnType() == boolean.class, "containsNS(String,String) boolean");
        // behavioural contract through a user implementation
        org.w3c.dom.NameList nl = new org.w3c.dom.NameList() {
            private final String[] names = {"alpha", "beta"};
            private final String[] uris = {null, "urn:x"};
            public int getLength() { return names.length; }
            public String getName(int i) { return i >= 0 && i < names.length ? names[i] : null; }
            public String getNamespaceURI(int i) { return i >= 0 && i < uris.length ? uris[i] : null; }
            public boolean contains(String str) { return "alpha".equals(str) || "beta".equals(str); }
            public boolean containsNS(String ns, String name) { return "urn:x".equals(ns) && "beta".equals(name); }
        };
        check(nl.getLength() == 2, "user NameList length");
        check("alpha".equals(nl.getName(0)) && "beta".equals(nl.getName(1)), "getName ordering");
        check(nl.getNamespaceURI(0) == null && "urn:x".equals(nl.getNamespaceURI(1)), "namespace lookup by index");
        check(nl.getName(2) == null && nl.getName(-1) == null, "out-of-range name null");
        check(nl.contains("alpha") && !nl.contains("zz"), "contains by name");
        check(nl.containsNS("urn:x", "beta") && !nl.containsNS("urn:x", "alpha"), "containsNS needs both ns and name");
        // reachability note: the JDK exposes no live NameList producer (DOMConfiguration
        // returns DOMStringList); NameList remains a user-implemented contract surface
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        check(!(d.getDomConfig().getParameterNames() instanceof org.w3c.dom.NameList), "JDK producer check: parameter names are DOMStringList, not NameList");
    }
}
