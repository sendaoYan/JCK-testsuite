/*
 * @test
 * @summary org.w3c.dom.xpath.XPathResult conformance: reflection shape checks.
 * @run main XPathResultTest
 */

import java.lang.reflect.*;

public class XPathResultTest {
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
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathResult");
        check(c != null, "org.w3c.dom.xpath.XPathResult loads");
        check(c.getName().equals("org.w3c.dom.xpath.XPathResult"), "binary name is org.w3c.dom.xpath.XPathResult");
        check(Modifier.isPublic(c.getModifiers()), "org.w3c.dom.xpath.XPathResult is public");
        check(c.isInterface(), "org.w3c.dom.xpath.XPathResult is an interface");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        check(declares(c, "getResultType"), "declares getResultType");
        check(declares(c, "getNumberValue"), "declares getNumberValue");
        check(declares(c, "getStringValue"), "declares getStringValue");
        check(declares(c, "getBooleanValue"), "declares getBooleanValue");
        check(declares(c, "getSingleNodeValue"), "declares getSingleNodeValue");
        check(declares(c, "getInvalidIteratorState"), "declares getInvalidIteratorState");
        deepContract();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- deepening round 2: XPathResult constant table (JDK 8 reflection) ----
    static void deepContract() throws Exception {
        Class<?> c = Class.forName("org.w3c.dom.xpath.XPathResult");
        String[] consts = {"ANY_TYPE","NUMBER_TYPE","STRING_TYPE","BOOLEAN_TYPE","UNORDERED_NODE_ITERATOR_TYPE",
            "ORDERED_NODE_ITERATOR_TYPE","UNORDERED_NODE_SNAPSHOT_TYPE","ORDERED_NODE_SNAPSHOT_TYPE","ANY_UNORDERED_NODE_TYPE","FIRST_ORDERED_NODE_TYPE"};
        for (int i = 0; i < consts.length; i++)
            check(c.getField(consts[i]).getShort(null) == i, consts[i] + " = " + i);
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) names.add(m.getName());
        check(names.contains("getResultType") && names.contains("getNumberValue") && names.contains("getStringValue"), "typed getters declared (number/string)");
        check(names.contains("getBooleanValue") && names.contains("getSingleNodeValue") && names.contains("getInvalidIteratorState"), "typed getters declared (bool/single/iterator-state)");
        check(names.contains("getSnapshotLength") && names.contains("iterateNext") && names.contains("snapshotItem"), "iteration/snapshot methods declared");
    }
}
