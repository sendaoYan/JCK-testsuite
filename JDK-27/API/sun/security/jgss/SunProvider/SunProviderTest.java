/*
 * @test
 * @summary sun.security.jgss.SunProvider conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main SunProviderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SunProviderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.SunProvider", false, SunProviderTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.SunProvider"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.security.Provider"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
