/*
 * @test
 * @summary sun.security.krb5.internal.HostAddresses conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main HostAddressesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class HostAddressesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.HostAddresses", false, HostAddressesTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.HostAddresses"), "name");
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
        check(methods.contains("getInetAddresses"), "method:getInetAddresses");
        check(methods.contains("getLocalAddresses"), "method:getLocalAddresses");
        check(methods.contains("hashCode"), "method:hashCode");
        check(methods.contains("inList"), "method:inList");
        check(methods.contains("parse"), "method:parse");
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("writeAddrs"), "method:writeAddrs");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
