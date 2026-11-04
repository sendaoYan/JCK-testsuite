/*
 * @test
 * @summary com.sun.corba.se.impl.oa.poa.ActiveObjectMap conformance: type shape via reflection (headless-safe).
 * @run main ActiveObjectMapTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ActiveObjectMapTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.oa.poa.ActiveObjectMap", false, ActiveObjectMapTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("contains"), "method contains");
        check(mn.contains("containsKey"), "method containsKey");
        check(mn.contains("create"), "method create");
        check(mn.contains("get"), "method get");
        check(mn.contains("getKey"), "method getKey");
        check(mn.contains("getServant"), "method getServant");
        check(mn.contains("hasMultipleIDs"), "method hasMultipleIDs");
        check(mn.contains("keySet"), "method keySet");
        check(mn.contains("putServant"), "method putServant");
        check(mn.contains("remove"), "method remove");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
