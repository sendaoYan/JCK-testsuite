/*
 * @test
 * @summary com.sun.corba.se.impl.naming.pcosnaming.PersistentBindingIterator conformance: type shape via reflection (headless-safe).
 * @run main PersistentBindingIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PersistentBindingIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.pcosnaming.PersistentBindingIterator", false, PersistentBindingIteratorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.impl.naming.cosnaming.BindingIteratorImpl"), "superclass com.sun.corba.se.impl.naming.cosnaming.BindingIteratorImpl");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("Destroy"), "method Destroy");
        check(mn.contains("NextOne"), "method NextOne");
        check(mn.contains("RemainingElements"), "method RemainingElements");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
