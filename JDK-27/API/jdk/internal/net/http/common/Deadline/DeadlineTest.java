/*
 * @test
 * @summary jdk.internal.net.http.common.Deadline conformance: internal package reflection.
 * @modules java.net.http/jdk.internal.net.http.common:+open
 * @run main DeadlineTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DeadlineTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.net.http.common.Deadline", false, DeadlineTest.class.getClassLoader());
        check(c.getName().equals("jdk.internal.net.http.common.Deadline"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Comparable"), "iface:java.lang.Comparable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("between"), "method:between");
        check(methods.contains("compareTo"), "method:compareTo");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isAfter"), "method:isAfter");
        check(methods.contains("isBefore"), "method:isBefore");
        check(methods.contains("minus"), "method:minus");
        check(methods.contains("plus"), "method:plus");
        check(methods.contains("plusMillis"), "method:plusMillis");
        check(methods.contains("plusNanos"), "method:plusNanos");
        check(methods.contains("plusSeconds"), "method:plusSeconds");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("truncatedTo"), "method:truncatedTo");
        check(methods.contains("until"), "method:until");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("MAX"), "field:MAX");
        check(fields.contains("MIN"), "field:MIN");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
