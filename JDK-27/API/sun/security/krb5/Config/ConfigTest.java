/*
 * @test
 * @summary sun.security.krb5.Config conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main ConfigTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ConfigTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.Config", false, ConfigTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.Config"), "name");
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
        check(methods.contains("defaultEtype"), "method:defaultEtype");
        check(methods.contains("duration"), "method:duration");
        check(methods.contains("exists"), "method:exists");
        check(methods.contains("get"), "method:get");
        check(methods.contains("getAll"), "method:getAll");
        check(methods.contains("getBooleanObject"), "method:getBooleanObject");
        check(methods.contains("getDefaultRealm"), "method:getDefaultRealm");
        check(methods.contains("getInstance"), "method:getInstance");
        check(methods.contains("getIntValue"), "method:getIntValue");
        check(methods.contains("getKDCList"), "method:getKDCList");
        check(methods.contains("getType"), "method:getType");
        check(methods.contains("listTable"), "method:listTable");
        check(methods.contains("refresh"), "method:refresh");
        check(methods.contains("resetDefaultRealm"), "method:resetDefaultRealm");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("useAddresses"), "method:useAddresses");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("DISABLE_REFERRALS"), "field:DISABLE_REFERRALS");
        check(fields.contains("MAX_REFERRALS"), "field:MAX_REFERRALS");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
