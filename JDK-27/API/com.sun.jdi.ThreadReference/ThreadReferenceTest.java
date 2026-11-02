/*
 * @test
 * @summary com.sun.jdi.ThreadReference conformance: interface hierarchy and methods.
 * @run main ThreadReferenceTest
 */

import java.lang.reflect.*;
import java.util.*;

public class ThreadReferenceTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.ThreadReference");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.ObjectReference"), "extends com.sun.jdi.ObjectReference");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("suspend"), "method suspend");
        check(dm.contains("resume"), "method resume");
        check(dm.contains("suspendCount"), "method suspendCount");
        check(dm.contains("stop"), "method stop");
        check(dm.contains("interrupt"), "method interrupt");
        check(dm.contains("status"), "method status");
        check(dm.contains("isSuspended"), "method isSuspended");
        check(dm.contains("isAtBreakpoint"), "method isAtBreakpoint");
        check(dm.contains("threadGroup"), "method threadGroup");
        check(dm.contains("frameCount"), "method frameCount");
        check(dm.contains("frames"), "method frames");
        check(dm.contains("frame"), "method frame");
        check(dm.contains("ownedMonitors"), "method ownedMonitors");
        check(dm.contains("ownedMonitorsAndFrames"), "method ownedMonitorsAndFrames");
        check(dm.contains("currentContendedMonitor"), "method currentContendedMonitor");
        check(dm.contains("popFrames"), "method popFrames");
        check(dm.contains("forceEarlyReturn"), "method forceEarlyReturn");
        check(dm.contains("isVirtual"), "method isVirtual");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
