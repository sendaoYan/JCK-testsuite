/*
 * @test
 * @summary jdk.internal.net.http.qpack.DynamicTable conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.qpack:+open
 * @run main DynamicTableTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DynamicTableTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.qpack.DynamicTable", false, DynamicTableTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.qpack.DynamicTable"), "name");
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
        check(methods.contains("availableEvictableSpace"), "method:availableEvictableSpace");
        check(methods.contains("awaitFutureInsertCount"), "method:awaitFutureInsertCount");
        check(methods.contains("capacity"), "method:capacity");
        check(methods.contains("cleanupStreamInsertCountNotifications"), "method:cleanupStreamInsertCountNotifications");
        check(methods.contains("duplicate"), "method:duplicate");
        check(methods.contains("duplicateWithEncoderStreamUpdate"), "method:duplicateWithEncoderStreamUpdate");
        check(methods.contains("get"), "method:get");
        check(methods.contains("getRelative"), "method:getRelative");
        check(methods.contains("headerSize"), "method:headerSize");
        check(methods.contains("insert"), "method:insert");
        check(methods.contains("insertCount"), "method:insertCount");
        check(methods.contains("insertWithEncoderStreamUpdate"), "method:insertWithEncoderStreamUpdate");
        check(methods.contains("maxCapacity"), "method:maxCapacity");
        check(methods.contains("maxEntries"), "method:maxEntries");
        check(methods.contains("search"), "method:search");
        check(methods.contains("setCapacity"), "method:setCapacity");
        check(methods.contains("setCapacityWithEncoderStreamUpdate"), "method:setCapacityWithEncoderStreamUpdate");
        check(methods.contains("setMaxTableCapacity"), "method:setMaxTableCapacity");
        check(methods.contains("size"), "method:size");
        check(methods.contains("toRelative"), "method:toRelative");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("tryReferenceEntry"), "method:tryReferenceEntry");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("ENTRY_SIZE"), "field:ENTRY_SIZE");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
