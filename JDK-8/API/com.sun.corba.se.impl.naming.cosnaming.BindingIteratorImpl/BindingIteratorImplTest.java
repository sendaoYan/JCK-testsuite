/*
 * @test
 * @summary com.sun.corba.se.impl.naming.cosnaming.BindingIteratorImpl conformance: type shape via reflection (headless-safe).
 * @run main BindingIteratorImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class BindingIteratorImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.naming.cosnaming.BindingIteratorImpl", false, BindingIteratorImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CosNaming.BindingIteratorPOA"), "superclass org.omg.CosNaming.BindingIteratorPOA");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("destroy"), "method destroy");
        check(mn.contains("list"), "method list");
        check(mn.contains("next_n"), "method next_n");
        check(mn.contains("next_one"), "method next_one");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
