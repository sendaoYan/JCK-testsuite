/*
 * @test
 * @summary com.sun.jdi.MonitorInfo conformance: white-box structure + black-box behavior.
 * @run main MonitorInfoTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class MonitorInfoTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.MonitorInfo");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 1, "direct interface count");
        check(si.contains("com.sun.jdi.Mirror"), "extends com.sun.jdi.Mirror");
        Set<String> dm = methodNames(c);
        check(dm.contains("monitor"), "method monitor");
        check(dm.contains("stackDepth"), "method stackDepth");
        check(dm.contains("thread"), "method thread");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        MonitorInfo mi = new MonitorInfo() {
            public ObjectReference monitor() { return null; }
            public int stackDepth() { return 0; }
            public ThreadReference thread() { return null; }
            public VirtualMachine virtualMachine() { return null; }
            public String toString() { return "mi"; }
        };
        check(mi.monitor() == null, "monitor");
        check(mi.stackDepth() == 0, "stackDepth");
        check(mi.thread() == null, "thread");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
