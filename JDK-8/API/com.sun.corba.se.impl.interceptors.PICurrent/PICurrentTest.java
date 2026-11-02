/*
 * @test
 * @summary com.sun.corba.se.impl.interceptors.PICurrent conformance: type shape via reflection (headless-safe).
 * @run main PICurrentTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PICurrentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.interceptors.PICurrent", false, PICurrentTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("org.omg.CORBA.LocalObject"), "superclass org.omg.CORBA.LocalObject");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("org.omg.PortableInterceptor.Current"), "interface org.omg.PortableInterceptor.Current");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 2, "public declared method count");
        check(mn.contains("get_slot"), "method get_slot");
        check(mn.contains("set_slot"), "method set_slot");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
