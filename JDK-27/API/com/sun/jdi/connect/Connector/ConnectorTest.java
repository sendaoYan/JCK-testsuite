/*
 * @test
 * @summary com.sun.jdi.connect.Connector conformance: white-box structure + black-box behavior.
 * @run main ConnectorTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class ConnectorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.connect.Connector");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 0, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("name"), "method name");
        check(dm.contains("description"), "method description");
        check(dm.contains("transport"), "method transport");
        check(dm.contains("defaultArguments"), "method defaultArguments");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        for (Connector cn : vmm.allConnectors()) {
            check(cn.name() != null && cn.name().length() > 0, "name");
            check(cn.description() != null && cn.description().length() > 0, "description");
            check(cn.transport() != null, "transport");
            java.util.Map<String, Connector.Argument> d = cn.defaultArguments();
            check(d.size() > 0, "defaultArguments non-empty");
        }

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
