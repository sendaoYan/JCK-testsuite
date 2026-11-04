/*
 * @test
 * @summary sun.security.jgss.GSSManagerImpl conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main GSSManagerImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class GSSManagerImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.GSSManagerImpl", false, GSSManagerImplTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.GSSManagerImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.ietf.jgss.GSSManager"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("addProviderAtEnd"), "method:addProviderAtEnd");
        check(methods.contains("addProviderAtFront"), "method:addProviderAtFront");
        check(methods.contains("createContext"), "method:createContext");
        check(methods.contains("createCredential"), "method:createCredential");
        check(methods.contains("createName"), "method:createName");
        check(methods.contains("getCredentialElement"), "method:getCredentialElement");
        check(methods.contains("getMechs"), "method:getMechs");
        check(methods.contains("getMechsForName"), "method:getMechsForName");
        check(methods.contains("getNameElement"), "method:getNameElement");
        check(methods.contains("getNamesForMech"), "method:getNamesForMech");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
