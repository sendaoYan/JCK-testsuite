/*
 * @test
 * @summary com.sun.corba.se.spi.presentation.rmi.PresentationManager conformance: type shape via reflection (headless-safe).
 * @run main PresentationManagerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class PresentationManagerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.presentation.rmi.PresentationManager", false, PresentationManagerTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 6, "public declared method count");
        check(mn.contains("getClassData"), "method getClassData");
        check(mn.contains("getDynamicMethodMarshaller"), "method getDynamicMethodMarshaller");
        check(mn.contains("getStubFactoryFactory"), "method getStubFactoryFactory");
        check(mn.contains("getTie"), "method getTie");
        check(mn.contains("setStubFactoryFactory"), "method setStubFactoryFactory");
        check(mn.contains("useDynamicStubs"), "method useDynamicStubs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
