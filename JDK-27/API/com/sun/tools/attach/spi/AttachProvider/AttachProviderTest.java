/*
 * @test
 * @summary com.sun.tools.attach.spi.AttachProvider conformance: white-box structure + black-box behavior.
 * @run main AttachProviderTest
 */

import java.lang.reflect.*;
import java.util.*;

public class AttachProviderTest {
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
        Class<?> c = load("com.sun.tools.attach.spi.AttachProvider");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        Set<String> m = methodNames(c);
        check(m.contains("name"), "name");
        check(m.contains("type"), "type");
        check(m.contains("attachVirtualMachine"), "attachVirtualMachine");
        check(m.contains("listVirtualMachines"), "listVirtualMachines");
        check(m.contains("providers"), "providers static");
        java.util.List<com.sun.tools.attach.spi.AttachProvider> ps = com.sun.tools.attach.spi.AttachProvider.providers();
        check(ps != null, "providers non-null");
        check(!ps.isEmpty(), "providers non-empty");
        for (com.sun.tools.attach.spi.AttachProvider p : ps) { check(p.name() != null, "provider name"); check(p.type() != null, "provider type"); }
    }

    static void testBlackBox() throws Exception {
        java.util.List<com.sun.tools.attach.spi.AttachProvider> ps = com.sun.tools.attach.spi.AttachProvider.providers();
        check(ps != null && !ps.isEmpty(), "providers non-empty");
        com.sun.tools.attach.spi.AttachProvider p = ps.get(0);
        check(p.name() != null, "name non-null");
        check(p.type() != null, "type non-null");
        java.util.List<com.sun.tools.attach.VirtualMachineDescriptor> vms = p.listVirtualMachines();
        check(vms != null, "listVirtualMachines non-null");
        for (com.sun.tools.attach.VirtualMachineDescriptor d : vms) check(d != null, "descriptor non-null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
