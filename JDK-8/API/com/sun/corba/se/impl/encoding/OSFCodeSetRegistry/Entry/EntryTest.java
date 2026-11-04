/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.OSFCodeSetRegistry.Entry conformance: type shape via reflection (headless-safe).
 * @run main EntryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class EntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.OSFCodeSetRegistry$Entry", false, EntryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("equals"), "method equals");
        check(mn.contains("getMaxBytesPerChar"), "method getMaxBytesPerChar");
        check(mn.contains("getName"), "method getName");
        check(mn.contains("getNumber"), "method getNumber");
        check(mn.contains("hashCode"), "method hashCode");
        check(mn.contains("isFixedWidth"), "method isFixedWidth");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
