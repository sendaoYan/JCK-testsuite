/*
 * @test
 * @summary com.sun.org.slf4j.internal.LoggerFactory conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.slf4j.internal:+open
 * @run main LoggerFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class LoggerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.slf4j.internal.LoggerFactory", false, LoggerFactoryTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.slf4j.internal.LoggerFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getLogger"), "method:getLogger");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
