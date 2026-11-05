/*
 * @test
 * @summary org.w3c.dom.NameList conformance: reflection shape checks.
 * @run main NameListTest
 */

import java.lang.reflect.*;

public class NameListTest {
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
        Class<?> c = Class.forName("org.w3c.dom.NameList");
        check(c != null, "org.w3c.dom.NameList loads");
        check(c.getName().equals("org.w3c.dom.NameList"), "binary name is org.w3c.dom.NameList");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.NameList is public");
        check(c.isInterface(), "org.w3c.dom.NameList is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getName"), "declares getName");
        check(declares(c, "getNamespaceURI"), "declares getNamespaceURI");
        check(declares(c, "getLength"), "declares getLength");
        check(declares(c, "contains"), "declares contains");
        check(declares(c, "containsNS"), "declares containsNS");
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
