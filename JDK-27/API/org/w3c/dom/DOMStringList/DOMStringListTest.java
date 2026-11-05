/*
 * @test
 * @summary org.w3c.dom.DOMStringList conformance: interface and methods.
 * @run main DOMStringListTest
 */

import org.w3c.dom.DOMStringList;

public class DOMStringListTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

        // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {

        check(DOMStringList.class.isInterface(), "DOMStringList is an interface");
        check(DOMStringList.class.getMethod("item", int.class)
                .getReturnType() == String.class, "item(int) returns String");
        check(DOMStringList.class.getMethod("getLength").getReturnType() == int.class,
              "getLength() returns int");
        check(DOMStringList.class.getMethod("contains", String.class)
                .getReturnType() == boolean.class, "contains(String) returns boolean");
    
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        org.w3c.dom.DOMStringList a = new org.w3c.dom.DOMStringList(){
            public java.lang.String item(int p0) { return "s"; }
            public int getLength() { return 3; }
            public boolean contains(java.lang.String p0) { return true; }
        };
        check(a != null, "anon impl of DOMStringList");
        check("s".equals(a.item(3)), "item() returns fixed default");
        check(a.getLength() == 3, "getLength() returns fixed default");
        check(a.contains("s") == true, "contains() returns fixed default");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: behavioral DOMStringList contract (interface + Xerces CCE path) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.DOMStringList");
        check(c.isInterface(), "DOMStringList is an interface");
        java.lang.reflect.Method item = c.getMethod("item", int.class);
        check(item.getReturnType() == String.class, "item(int) returns String");
        check(c.getMethod("getLength").getReturnType() == int.class, "getLength() returns int");
        check(c.getMethod("contains", String.class).getReturnType() == boolean.class, "contains(String) returns boolean");
        // a user implementation honours the contract (live JDK usage pattern)
        org.w3c.dom.DOMStringList list = new org.w3c.dom.DOMStringList() {
            private final java.util.List<String> data = java.util.Arrays.asList("a", "b", "c");
            public String item(int index) { return index >= 0 && index < data.size() ? data.get(index) : null; }
            public int getLength() { return data.size(); }
            public boolean contains(String str) { return data.contains(str); }
        };
        check(list.getLength() == 3, "user list length");
        check("a".equals(list.item(0)) && "c".equals(list.item(2)), "item(i) in order");
        check(list.item(3) == null && list.item(-1) == null, "out-of-range item null");
        check(list.contains("b") && !list.contains("z"), "contains semantics");
        check(!list.contains(null), "contains(null) false for this implementation");
        // live JDK producer: the document configuration parameter-name list
        javax.xml.parsers.DocumentBuilderFactory f = javax.xml.parsers.DocumentBuilderFactory.newInstance();
        org.w3c.dom.Document d = f.newDocumentBuilder().newDocument();
        org.w3c.dom.DOMStringList live = (org.w3c.dom.DOMStringList) d.getDomConfig().getParameterNames();
        check(live.getLength() >= 15, "live list: document configuration exposes many parameters");
        check(live.contains("comments") && live.contains("entities"), "live list: contains reports known names");
        check(!live.contains("nope"), "live list: contains rejects unknown names");
        check(live.item(0) != null, "live list: item(0) resolves");
        // on JDK 8 the underlying Xerces list class is reflectively reachable; module system blocks it on 9+
        boolean is8 = System.getProperty("java.version").startsWith("1.8");
        if (is8) {
            Class<?> impl = Class.forName("com.sun.org.apache.xerces.internal.dom.DOMStringListImpl");
            Object inst = impl.getDeclaredConstructor().newInstance();
            org.w3c.dom.DOMStringList real = (org.w3c.dom.DOMStringList) inst;
            check(real.getLength() == 0 && real.item(0) == null, "JDK 8: empty Xerces DOMStringList behaves per contract");
        } else {
            boolean blocked = false;
            try { Class.forName("com.sun.org.apache.xerces.internal.dom.DOMStringListImpl").getDeclaredConstructor().newInstance(); }
            catch (Throwable expected) { blocked = true; }
            check(blocked, "JDK 27: reflective construction of the internal list is blocked by the module system");
        }
    }
}
