/*
 * @test
 * @summary sun.security.jgss.krb5.Krb5Util conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.krb5:+open
 * @run main Krb5UtilTest
 */
import java.lang.reflect.*;
import java.util.*;
public class Krb5UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.krb5.Krb5Util", false, Krb5UtilTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.krb5.Krb5Util"), "name");
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
        check(methods.contains("credsToTicket"), "method:credsToTicket");
        check(methods.contains("getServiceCreds"), "method:getServiceCreds");
        check(methods.contains("keyInfo"), "method:keyInfo");
        check(methods.contains("keysFromJavaxKeyTab"), "method:keysFromJavaxKeyTab");
        check(methods.contains("snapshotFromJavaxKeyTab"), "method:snapshotFromJavaxKeyTab");
        check(methods.contains("ticketToCreds"), "method:ticketToCreds");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
