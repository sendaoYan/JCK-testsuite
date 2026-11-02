/*
 * @test
 * @summary sun.security.krb5.Realm conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5:+open
 * @run main RealmTest
 */
import java.lang.reflect.*;
import java.util.*;
public class RealmTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.Realm", false, RealmTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.Realm"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.lang.Cloneable"), "iface:java.lang.Cloneable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asn1Encode"), "method:asn1Encode");
        check(methods.contains("clone"), "method:clone");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getDefault"), "method:getDefault");
        check(methods.contains("getRealmsList"), "method:getRealmsList");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("isValidRealmString"), "method:isValidRealmString");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("parseRealm"), "method:parseRealm");
        check(methods.contains("parseRealmAtSeparator"), "method:parseRealmAtSeparator");
        check(methods.contains("parseRealmComponent"), "method:parseRealmComponent");
        check(methods.contains("toString"), "method:toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
