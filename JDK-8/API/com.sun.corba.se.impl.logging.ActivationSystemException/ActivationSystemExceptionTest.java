/*
 * @test
 * @summary com.sun.corba.se.impl.logging.ActivationSystemException conformance: type shape via reflection (headless-safe).
 * @run main ActivationSystemExceptionTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class ActivationSystemExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.impl.logging.ActivationSystemException", false, ActivationSystemExceptionTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("com.sun.corba.se.spi.logging.LogWrapperBase"), "superclass com.sun.corba.se.spi.logging.LogWrapperBase");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 8, "public declared method count");
        check(mn.contains("cannotAddInitialNaming"), "method cannotAddInitialNaming");
        check(mn.contains("cannotReadRepositoryDb"), "method cannotReadRepositoryDb");
        check(mn.contains("cannotWriteRepositoryDb"), "method cannotWriteRepositoryDb");
        check(mn.contains("errorInBadServerIdHandler"), "method errorInBadServerIdHandler");
        check(mn.contains("get"), "method get");
        check(mn.contains("serverNotExpectedToRegister"), "method serverNotExpectedToRegister");
        check(mn.contains("serverNotRunning"), "method serverNotRunning");
        check(mn.contains("unableToStartProcess"), "method unableToStartProcess");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
