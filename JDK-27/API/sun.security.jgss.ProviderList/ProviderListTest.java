/*
 * @test
 * @summary sun.security.jgss.ProviderList conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main ProviderListTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ProviderListTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.ProviderList", false, ProviderListTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.ProviderList"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addProviderAtEnd"), "method:addProviderAtEnd");
        check(methods.contains("addProviderAtFront"), "method:addProviderAtFront");
        check(methods.contains("getMechFactory"), "method:getMechFactory");
        check(methods.contains("getMechs"), "method:getMechs");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DEFAULT_MECH_OID"), "field:DEFAULT_MECH_OID");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
