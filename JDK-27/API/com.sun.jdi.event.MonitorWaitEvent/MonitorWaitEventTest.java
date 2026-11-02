/*
 * @test
 * @summary com.sun.jdi.event.MonitorWaitEvent conformance: interface hierarchy and methods.
 * @run main MonitorWaitEventTest
 */

import java.lang.reflect.*;
import java.util.*;

public class MonitorWaitEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.event.MonitorWaitEvent");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.event.LocatableEvent"), "extends com.sun.jdi.event.LocatableEvent");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("thread"), "method thread");
        check(dm.contains("monitor"), "method monitor");
        check(dm.contains("timeout"), "method timeout");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
