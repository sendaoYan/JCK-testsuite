/*
 * @test
 * @summary com.sun.jdi.ObjectReference conformance: interface hierarchy and methods.
 * @run main ObjectReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ObjectReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ObjectReference");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Value"), "extends com.sun.jdi.Value");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("referenceType"), "method referenceType");
        check(dm.contains("getValue"), "method getValue");
        check(dm.contains("getValues"), "method getValues");
        check(dm.contains("setValue"), "method setValue");
        check(dm.contains("invokeMethod"), "method invokeMethod");
        check(dm.contains("disableCollection"), "method disableCollection");
        check(dm.contains("enableCollection"), "method enableCollection");
        check(dm.contains("isCollected"), "method isCollected");
        check(dm.contains("uniqueID"), "method uniqueID");
        check(dm.contains("waitingThreads"), "method waitingThreads");
        check(dm.contains("owningThread"), "method owningThread");
        check(dm.contains("entryCount"), "method entryCount");
        check(dm.contains("referringObjects"), "method referringObjects");
        check(dm.contains("equals"), "method equals");
        check(dm.contains("hashCode"), "method hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
