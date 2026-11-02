/*
 * @test
 * @summary com.sun.jdi.request.MonitorWaitedRequest conformance: interface hierarchy and methods.
 * @run main MonitorWaitedRequestTest
 */

import java.lang.reflect.*;
import java.util.*;

public class MonitorWaitedRequestTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.request.MonitorWaitedRequest");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.request.EventRequest"), "extends com.sun.jdi.request.EventRequest");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("addThreadFilter"), "method addThreadFilter");
        check(dm.contains("addClassFilter"), "method addClassFilter");
        check(dm.contains("addClassExclusionFilter"), "method addClassExclusionFilter");
        check(dm.contains("addInstanceFilter"), "method addInstanceFilter");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
