/*
 * @test
 * @summary com.sun.jdi.connect.ListeningConnector conformance: white-box structure + black-box behavior.
 * @run main ListeningConnectorTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class ListeningConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.ListeningConnector");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 1, "direct interface count");
        check(si.contains("com.sun.jdi.connect.Connector"), "extends com.sun.jdi.connect.Connector");
        Set<String> dm = methodNames(c);
        check(dm.contains("supportsMultipleConnections"), "method supportsMultipleConnections");
        check(dm.contains("startListening"), "method startListening");
        check(dm.contains("stopListening"), "method stopListening");
        check(dm.contains("accept"), "method accept");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        for (ListeningConnector lc : vmm.listeningConnectors()) {
            check(lc.name() != null, "name");
            lc.supportsMultipleConnections();
            check(true, "supportsMultipleConnections callable");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
