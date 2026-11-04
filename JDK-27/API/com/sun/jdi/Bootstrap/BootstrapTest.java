/*
 * @test
 * @summary com.sun.jdi.Bootstrap conformance: white-box structure + black-box behavior.
 * @run main BootstrapTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class BootstrapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.Bootstrap");
        check(!c.isInterface(), "isClass");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(c.getDeclaredConstructors().length == 1, "constructor count");
        Set<String> dm = methodNames(c);
        check(dm.contains("virtualMachineManager"), "method virtualMachineManager");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VirtualMachineManager vmm1 = Bootstrap.virtualMachineManager();
        check(vmm1 != null, "non-null");
        VirtualMachineManager vmm2 = Bootstrap.virtualMachineManager();
        check(vmm1 == vmm2, "singleton (synchronized)");
        check(vmm1.majorInterfaceVersion() > 0, "majorInterfaceVersion");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
