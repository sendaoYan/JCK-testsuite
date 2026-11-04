/*
 * @test
 * @summary com.sun.jdi.VirtualMachineManager conformance: white-box structure + black-box behavior.
 * @run main VirtualMachineManagerTest
 */

import com.sun.jdi.*;
import com.sun.jdi.connect.*;
import com.sun.jdi.connect.spi.*;
import com.sun.jdi.event.*;
import com.sun.jdi.request.*;
import java.util.*;

public class VirtualMachineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.VirtualMachineManager");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.size() == 0, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("defaultConnector"), "method defaultConnector");
        check(dm.contains("launchingConnectors"), "method launchingConnectors");
        check(dm.contains("attachingConnectors"), "method attachingConnectors");
        check(dm.contains("listeningConnectors"), "method listeningConnectors");
        check(dm.contains("allConnectors"), "method allConnectors");
        check(dm.contains("connectedVirtualMachines"), "method connectedVirtualMachines");
        check(dm.contains("majorInterfaceVersion"), "method majorInterfaceVersion");
        check(dm.contains("minorInterfaceVersion"), "method minorInterfaceVersion");
        check(dm.contains("createVirtualMachine"), "method createVirtualMachine");
    }

    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        VirtualMachineManager vmm = Bootstrap.virtualMachineManager();
        check(vmm.majorInterfaceVersion() > 0, "majorInterfaceVersion");
        check(vmm.minorInterfaceVersion() >= 0, "minorInterfaceVersion");
        check(vmm.allConnectors().size() > 0, "allConnectors non-empty");
        check(vmm.launchingConnectors().size() > 0, "launchingConnectors non-empty");
        check(vmm.attachingConnectors().size() > 0, "attachingConnectors non-empty");
        check(vmm.listeningConnectors().size() > 0, "listeningConnectors non-empty");
        check(vmm.defaultConnector() != null, "defaultConnector");
        check(vmm.defaultConnector().name().equals("com.sun.jdi.CommandLineLaunch"), "defaultConnector name");
        check(vmm.connectedVirtualMachines().size() >= 0, "connectedVirtualMachines callable");

    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
