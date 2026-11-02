/*
 * @test
 * @summary com.sun.tools.attach.VirtualMachineDescriptor conformance: descriptor accessors.
 * @run main VirtualMachineDescriptorTest
 */

import java.lang.reflect.*;
import java.util.*;

public class VirtualMachineDescriptorTest {
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

    public static void main(String[] a) throws Exception {
        com.sun.tools.attach.spi.AttachProvider p = com.sun.tools.attach.spi.AttachProvider.providers().get(0);
        check(p != null, "provider non-null");
        com.sun.tools.attach.VirtualMachineDescriptor d = new com.sun.tools.attach.VirtualMachineDescriptor(p, "123", "MyVM");
        check(d.id().equals("123"), "id");
        check(d.displayName().equals("MyVM"), "displayName");
        check(d.provider() == p, "provider");
        check(d.toString() != null, "toString");
        com.sun.tools.attach.VirtualMachineDescriptor d2 = new com.sun.tools.attach.VirtualMachineDescriptor(p, "123", "MyVM");
        check(d.equals(d2), "equals");
        check(d.hashCode() == d2.hashCode(), "hashCode");
        com.sun.tools.attach.VirtualMachineDescriptor d3 = new com.sun.tools.attach.VirtualMachineDescriptor(p, "123");
        check(d3.id().equals("123"), "id 2-arg");
        check(d3.provider() == p, "provider 2-arg");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
