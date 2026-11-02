/*
 * @test
 * @summary com.sun.jdi.event.MethodExitEvent conformance: interface hierarchy and methods.
 * @run main MethodExitEventTest
 */

import java.lang.reflect.*;
import java.util.*;

public class MethodExitEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.event.MethodExitEvent");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.event.LocatableEvent"), "extends com.sun.jdi.event.LocatableEvent");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("method"), "method method");
        check(dm.contains("returnValue"), "method returnValue");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
