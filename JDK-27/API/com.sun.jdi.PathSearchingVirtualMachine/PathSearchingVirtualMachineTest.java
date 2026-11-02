/*
 * @test
 * @summary com.sun.jdi.PathSearchingVirtualMachine conformance: interface hierarchy and methods.
 * @run main PathSearchingVirtualMachineTest
 */

import java.lang.reflect.*;
import java.util.*;

public class PathSearchingVirtualMachineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }

    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("com.sun.jdi.PathSearchingVirtualMachine");
        check(c.isInterface(), "isInterface");
        Set<String> si = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) si.add(i.getName());
        check(si.contains("com.sun.jdi.VirtualMachine"), "extends com.sun.jdi.VirtualMachine");
        check(si.size() == 1, "direct interface count");
        Set<String> dm = methodNames(c);
        check(dm.contains("classPath"), "method classPath");
        check(dm.contains("bootClassPath"), "method bootClassPath");
        check(dm.contains("baseDirectory"), "method baseDirectory");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
