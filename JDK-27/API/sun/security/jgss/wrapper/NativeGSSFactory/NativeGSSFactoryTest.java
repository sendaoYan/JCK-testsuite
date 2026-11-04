/*
 * @test
 * @summary sun.security.jgss.wrapper.NativeGSSFactory conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.wrapper:+open
 * @run main NativeGSSFactoryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class NativeGSSFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.wrapper.NativeGSSFactory", false, NativeGSSFactoryTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.wrapper.NativeGSSFactory"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("sun.security.jgss.spi.MechanismFactory"), "iface:sun.security.jgss.spi.MechanismFactory");
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
        check(methods.contains("setMech"), "method:setMech");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
