/*
 * @test
 * @summary sun.security.jgss.GSSHeader conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSHeaderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSHeader", false, GSSHeaderTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSHeader"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("encode"), "method:encode");
        check(methods.contains("getLength"), "method:getLength");
        check(methods.contains("getMaxMechTokenSize"), "method:getMaxMechTokenSize");
        check(methods.contains("getMechTokenLength"), "method:getMechTokenLength");
        check(methods.contains("getOid"), "method:getOid");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("TOKEN_ID"), "field:TOKEN_ID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
