/*
 * @test
 * @summary sun.security.jgss.LoginConfigImpl conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss:+open
 * @run main LoginConfigImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class LoginConfigImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.LoginConfigImpl", false, LoginConfigImplTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.LoginConfigImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("javax.security.auth.login.Configuration"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("getAppConfigurationEntry"), "method:getAppConfigurationEntry");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("HTTP_USE_GLOBAL_CREDS"), "field:HTTP_USE_GLOBAL_CREDS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
