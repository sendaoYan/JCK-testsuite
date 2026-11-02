/*
 * @test
 * @summary sun.security.jgss.spnego.NegTokenInit conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.spnego:+open
 * @run main NegTokenInitTest
 */
import java.lang.reflect.*;
import java.util.*;
public class NegTokenInitTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spnego.NegTokenInit", false, NegTokenInitTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.spnego.NegTokenInit"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.jgss.spnego.SpNegoToken"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getMechToken"), "method:getMechToken");
        check(methods.contains("getMechTypeList"), "method:getMechTypeList");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
