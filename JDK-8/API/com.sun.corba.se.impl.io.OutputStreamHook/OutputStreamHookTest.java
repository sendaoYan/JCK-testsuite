/*
 * @test
 * @summary com.sun.corba.se.impl.io.OutputStreamHook conformance: type shape via reflection (headless-safe).
 * @run main OutputStreamHookTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class OutputStreamHookTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.io.OutputStreamHook", false, OutputStreamHookTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.io.ObjectOutputStream"), "superclass java.io.ObjectOutputStream");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("defaultWriteObject"), "method defaultWriteObject");
        check(mn.contains("defaultWriteObjectDelegate"), "method defaultWriteObjectDelegate");
        check(mn.contains("getStreamFormatVersion"), "method getStreamFormatVersion");
        check(mn.contains("putFields"), "method putFields");
        check(mn.contains("writeFields"), "method writeFields");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
