/*
 * @test
 * @summary jdk.internal.net.http.common.BufferSupplier conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main BufferSupplierTest
 */
import java.lang.reflect.*;
import java.util.*;
public class BufferSupplierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.BufferSupplier", false, BufferSupplierTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.BufferSupplier"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.util.function.Supplier"), "iface:java.util.function.Supplier");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("get"), "method:get");
        check(methods.contains("recycle"), "method:recycle");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
