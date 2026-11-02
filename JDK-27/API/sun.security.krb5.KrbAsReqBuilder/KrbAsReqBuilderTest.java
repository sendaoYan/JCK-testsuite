/*
 * @test
 * @summary sun.security.krb5.KrbAsReqBuilder conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main KrbAsReqBuilderTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KrbAsReqBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.KrbAsReqBuilder", false, KrbAsReqBuilderTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.KrbAsReqBuilder"), "name");
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
        check(methods.contains("action"), "method:action");
        check(methods.contains("destroy"), "method:destroy");
        check(methods.contains("getCCreds"), "method:getCCreds");
        check(methods.contains("getCreds"), "method:getCreds");
        check(methods.contains("getKeys"), "method:getKeys");
        check(methods.contains("setAddresses"), "method:setAddresses");
        check(methods.contains("setOptions"), "method:setOptions");
        check(methods.contains("setRTime"), "method:setRTime");
        check(methods.contains("setTarget"), "method:setTarget");
        check(methods.contains("setTill"), "method:setTill");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
