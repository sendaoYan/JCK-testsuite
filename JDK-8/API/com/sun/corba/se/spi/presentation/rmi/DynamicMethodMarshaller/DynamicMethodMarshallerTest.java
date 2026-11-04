/*
 * @test
 * @summary com.sun.corba.se.spi.presentation.rmi.DynamicMethodMarshaller conformance: type shape via reflection (headless-safe).
 * @run main DynamicMethodMarshallerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class DynamicMethodMarshallerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.presentation.rmi.DynamicMethodMarshaller", false, DynamicMethodMarshallerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 10, "public declared method count");
        check(mn.contains("copyArguments"), "method copyArguments");
        check(mn.contains("copyResult"), "method copyResult");
        check(mn.contains("getMethod"), "method getMethod");
        check(mn.contains("isDeclaredException"), "method isDeclaredException");
        check(mn.contains("readArguments"), "method readArguments");
        check(mn.contains("readException"), "method readException");
        check(mn.contains("readResult"), "method readResult");
        check(mn.contains("writeArguments"), "method writeArguments");
        check(mn.contains("writeException"), "method writeException");
        check(mn.contains("writeResult"), "method writeResult");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
