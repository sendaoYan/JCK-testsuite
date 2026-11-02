/*
 * @test
 * @summary com.sun.jdi.event.EventSet conformance: interface hierarchy and methods.
 * @run main EventSetTest
 */

import java.lang.reflect.*;
import java.util.*;

public class EventSetTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.event.EventSet");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        check(si.contains("java.util.Set"), "extends java.util.Set");
        check(si.size() == 2, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("suspendPolicy"), "method suspendPolicy");
        check(dm.contains("eventIterator"), "method eventIterator");
        check(dm.contains("resume"), "method resume");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
