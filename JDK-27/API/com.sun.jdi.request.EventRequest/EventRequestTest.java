/*
 * @test
 * @summary com.sun.jdi.request.EventRequest conformance: interface hierarchy and methods.
 * @run main EventRequestTest
 */

import java.lang.reflect.*;
import java.util.*;

public class EventRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.request.EventRequest");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("isEnabled"), "method isEnabled");
        check(dm.contains("setEnabled"), "method setEnabled");
        check(dm.contains("enable"), "method enable");
        check(dm.contains("disable"), "method disable");
        check(dm.contains("addCountFilter"), "method addCountFilter");
        check(dm.contains("setSuspendPolicy"), "method setSuspendPolicy");
        check(dm.contains("suspendPolicy"), "method suspendPolicy");
        check(dm.contains("putProperty"), "method putProperty");
        check(dm.contains("getProperty"), "method getProperty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
