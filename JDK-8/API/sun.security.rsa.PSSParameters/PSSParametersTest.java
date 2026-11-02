/*
 * @test
 * @summary sun.security.rsa.PSSParameters conformance: type shape via reflection (headless-safe).
 * @run main PSSParametersTest
 */

import java.lang.reflect.Modifier;

public class PSSParametersTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.rsa.PSSParameters", false, PSSParametersTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.security.AlgorithmParametersSpi"), "superclass");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
