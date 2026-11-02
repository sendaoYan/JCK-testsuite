/*
 * @test
 * @summary sun.security.krb5.internal.KRBError conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main KRBErrorTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KRBErrorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.KRBError", false, KRBErrorTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.KRBError"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> ifaces = new HashSet<>();
        for (Class<?> k : c.getInterfaces()) ifaces.add(k.getName());
        check(ifaces.contains("java.io.Serializable"), "iface:java.io.Serializable");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("asn1Encode"), "method:asn1Encode");
        check(methods.contains("equals"), "method:equals");
        check(methods.contains("getClientMicroSeconds"), "method:getClientMicroSeconds");
        check(methods.contains("getClientRealm"), "method:getClientRealm");
        check(methods.contains("getClientTime"), "method:getClientTime");
        check(methods.contains("getErrorCode"), "method:getErrorCode");
        check(methods.contains("getErrorString"), "method:getErrorString");
        check(methods.contains("getPA"), "method:getPA");
        check(methods.contains("getServerMicroSeconds"), "method:getServerMicroSeconds");
        check(methods.contains("getServerTime"), "method:getServerTime");
        check(methods.contains("hashCode"), "method:hashCode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
