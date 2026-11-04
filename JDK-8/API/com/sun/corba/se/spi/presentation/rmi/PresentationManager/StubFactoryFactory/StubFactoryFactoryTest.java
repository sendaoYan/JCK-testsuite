/*
 * @test
 * @summary com.sun.corba.se.spi.presentation.rmi.PresentationManager.StubFactoryFactory conformance: type shape via reflection (headless-safe).
 * @run main StubFactoryFactoryTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class StubFactoryFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.presentation.rmi.PresentationManager$StubFactoryFactory", false, StubFactoryFactoryTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 4, "public declared method count");
        check(mn.contains("createStubFactory"), "method createStubFactory");
        check(mn.contains("createsDynamicStubs"), "method createsDynamicStubs");
        check(mn.contains("getStubName"), "method getStubName");
        check(mn.contains("getTie"), "method getTie");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
