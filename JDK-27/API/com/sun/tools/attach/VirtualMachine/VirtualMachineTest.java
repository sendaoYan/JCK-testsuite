/*
 * @test
 * @summary com.sun.tools.attach.VirtualMachine conformance: white-box structure + black-box behavior.
 * @key headful
 * @run main VirtualMachineTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VirtualMachineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Set<String> methodNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) s.add(m.getName());
        return s;
    }
    static Set<String> fieldNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Field f : c.getDeclaredFields()) s.add(f.getName());
        return s;
    }
    static Set<String> interfaces(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }
    static Class<?> load(String fqcn) throws Exception { return Class.forName(fqcn); }

    static void testWhiteBox() throws Exception {
        Class<?> c = load("com.sun.tools.attach.VirtualMachine");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        String[] names = {"list","attach","detach","provider","id","loadAgentLibrary","loadAgentPath","loadAgent","getSystemProperties","getAgentProperties","startManagementAgent","startLocalManagementAgent"};
        for (String n : names) check(m.contains(n), n);
        java.util.List<com.sun.tools.attach.VirtualMachineDescriptor> ds = com.sun.tools.attach.VirtualMachine.list();
        check(ds != null, "list non-null");
        for (com.sun.tools.attach.VirtualMachineDescriptor d : ds) { check(d.id() != null, "id non-null"); check(d.displayName() != null, "displayName non-null"); check(d.provider() != null, "provider non-null"); }
    }

    static void testBlackBox() throws Exception {
        java.util.List<com.sun.tools.attach.VirtualMachineDescriptor> ds = com.sun.tools.attach.VirtualMachine.list();
        check(ds != null, "list non-null");
        for (com.sun.tools.attach.VirtualMachineDescriptor d : ds) {
            check(d.id() != null && !d.id().isEmpty(), "id non-empty");
            check(d.displayName() != null, "displayName non-null");
            check(d.provider() != null, "provider non-null");
        }
        // list() is a live snapshot of running JVMs; under concurrent test
        // execution other VMs attach/detach between calls, so its size is not
        // required to be stable. Re-fetching must still be non-null.
        check(com.sun.tools.attach.VirtualMachine.list() != null, "list non-null on re-fetch");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
