/*
 * @test
 * @summary com.sun.corba.se.impl.encoding.CachedCodeBase conformance: type shape via reflection (headless-safe).
 * @run main CachedCodeBaseTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CachedCodeBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.encoding.CachedCodeBase", false, CachedCodeBaseTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.org.omg.SendingContext._CodeBaseImplBase"), "superclass com.sun.org.omg.SendingContext._CodeBaseImplBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 7, "public declared method count");
        check(mn.contains("bases"), "method bases");
        check(mn.contains("cleanCache"), "method cleanCache");
        check(mn.contains("get_ir"), "method get_ir");
        check(mn.contains("implementation"), "method implementation");
        check(mn.contains("implementations"), "method implementations");
        check(mn.contains("meta"), "method meta");
        check(mn.contains("metas"), "method metas");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
