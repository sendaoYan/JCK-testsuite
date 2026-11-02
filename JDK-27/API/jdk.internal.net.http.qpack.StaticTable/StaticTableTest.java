/*
 * @test
 * @summary jdk.internal.net.http.qpack.StaticTable conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main StaticTableTest
 */
import java.lang.reflect.*;
import java.util.*;
public class StaticTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.StaticTable", false, StaticTableTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.StaticTable"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("jdk.internal.net.http.qpack.HeadersTable"), "iface:jdk.internal.net.http.qpack.HeadersTable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("get"), "method:get");
        check(methods.contains("insert"), "method:insert");
        check(methods.contains("search"), "method:search");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("HTTP3"), "field:HTTP3");
        check(fields.contains("HTTP3_HEADER_FIELDS"), "field:HTTP3_HEADER_FIELDS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
