/*
 * @test
 * @summary sun.security.jgss.spi.MechanismFactory conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.spi:+open
 * @run main MechanismFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class MechanismFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.spi.MechanismFactory", false, MechanismFactoryTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.spi.MechanismFactory"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getCredentialElement"), "method:getCredentialElement");
        check(methods.contains("getMechanismContext"), "method:getMechanismContext");
        check(methods.contains("getMechanismOid"), "method:getMechanismOid");
        check(methods.contains("getNameElement"), "method:getNameElement");
        check(methods.contains("getNameTypes"), "method:getNameTypes");
        check(methods.contains("getProvider"), "method:getProvider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
