/*
 * @test
 * @summary com.sun.corba.se.impl.presentation.rmi.ExceptionHandlerImpl conformance: type shape via reflection (headless-safe).
 * @run main ExceptionHandlerImplTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ExceptionHandlerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.presentation.rmi.ExceptionHandlerImpl", false, ExceptionHandlerImplTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.impl.presentation.rmi.ExceptionHandler"), "interface com.sun.corba.se.impl.presentation.rmi.ExceptionHandler");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("getRMIExceptionRW"), "method getRMIExceptionRW");
        check(mn.contains("isDeclaredException"), "method isDeclaredException");
        check(mn.contains("readException"), "method readException");
        check(mn.contains("writeException"), "method writeException");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
