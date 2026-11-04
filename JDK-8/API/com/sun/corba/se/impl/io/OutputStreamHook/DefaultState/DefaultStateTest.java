/*
 * @test
 * @summary com.sun.corba.se.impl.io.OutputStreamHook.DefaultState conformance: type shape via reflection (headless-safe).
 * @run main DefaultStateTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DefaultStateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.OutputStreamHook$DefaultState", false, DefaultStateTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.io.OutputStreamHook$WriteObjectState"), "superclass com.sun.corba.se.impl.io.OutputStreamHook$WriteObjectState");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 1, "public declared method count");
        check(mn.contains("enterWriteObject"), "method enterWriteObject");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
