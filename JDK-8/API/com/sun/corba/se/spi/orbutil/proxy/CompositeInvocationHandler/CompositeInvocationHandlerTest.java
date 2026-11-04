/*
 * @test
 * @summary com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandler conformance: type shape via reflection (headless-safe).
 * @run main CompositeInvocationHandlerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CompositeInvocationHandlerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandler", false, CompositeInvocationHandlerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("java.io.Serializable"), "interface java.io.Serializable");
        check(in.contains("java.lang.reflect.InvocationHandler"), "interface java.lang.reflect.InvocationHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 2, "public declared method count");
        check(mn.contains("addInvocationHandler"), "method addInvocationHandler");
        check(mn.contains("setDefaultHandler"), "method setDefaultHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
