/*
 * @test
 * @summary com.sun.jdi.connect.LaunchingConnector conformance: white-box structure + black-box behavior.
 * @run main LaunchingConnectorTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class LaunchingConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.LaunchingConnector");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 1, "direct interface count");
        check(si.contains("com.sun.jdi.connect.Connector"), "extends com.sun.jdi.connect.Connector");
        Set<String> dm = methodNames(c);
        check(dm.contains("launch"), "method launch");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        for (LaunchingConnector lc : vmm.launchingConnectors()) {
            check(lc.name() != null, "name");
            check(lc.defaultArguments().size() > 0, "defaultArguments non-empty");
        }
        java.util.Map<String, Connector.Argument> d = vmm.defaultConnector().defaultArguments();
        check(d.containsKey("main"), "main argument");
        check(d.containsKey("options"), "options argument");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
