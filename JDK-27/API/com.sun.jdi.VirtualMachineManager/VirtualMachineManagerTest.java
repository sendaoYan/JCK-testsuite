/*
 * @test
 * @summary com.sun.jdi.VirtualMachineManager conformance: interface hierarchy and methods.
 * @run main VirtualMachineManagerTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VirtualMachineManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
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
        System.out.println("PASS (" + passed + " assertions)");
    }
}
