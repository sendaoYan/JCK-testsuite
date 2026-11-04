/*
 * @test
 * @summary com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandlerImpl conformance: type shape via reflection (headless-safe).
 * @run main CompositeInvocationHandlerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CompositeInvocationHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandlerImpl", false, CompositeInvocationHandlerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandler"), "interface com.sun.corba.se.spi.orbutil.proxy.CompositeInvocationHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 3, "public declared method count");
        check(mn.contains("addInvocationHandler"), "method addInvocationHandler");
        check(mn.contains("invoke"), "method invoke");
        check(mn.contains("setDefaultHandler"), "method setDefaultHandler");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
