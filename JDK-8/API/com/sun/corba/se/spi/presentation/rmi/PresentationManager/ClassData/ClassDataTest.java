/*
 * @test
 * @summary com.sun.corba.se.spi.presentation.rmi.PresentationManager.ClassData conformance: type shape via reflection (headless-safe).
 * @run main ClassDataTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ClassDataTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.presentation.rmi.PresentationManager$ClassData", false, ClassDataTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 5, "public declared method count");
        check(mn.contains("getDictionary"), "method getDictionary");
        check(mn.contains("getIDLNameTranslator"), "method getIDLNameTranslator");
        check(mn.contains("getInvocationHandlerFactory"), "method getInvocationHandlerFactory");
        check(mn.contains("getMyClass"), "method getMyClass");
        check(mn.contains("getTypeIds"), "method getTypeIds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
