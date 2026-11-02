/*
 * @test
 * @summary com.sun.corba.se.impl.corba.NamedValueImpl conformance: type shape via reflection (headless-safe).
 * @run main NamedValueImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class NamedValueImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.corba.NamedValueImpl", false, NamedValueImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.NamedValue"), "superclass org.omg.CORBA.NamedValue");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("flags"), "method flags");
        check(mn.contains("name"), "method name");
        check(mn.contains("value"), "method value");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
