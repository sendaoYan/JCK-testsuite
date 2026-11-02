/*
 * @test
 * @summary sun.security.krb5.internal.AuthorizationDataEntry conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main AuthorizationDataEntryTest
 */
import java.lang.reflect.*;
import java.util.*;
public class AuthorizationDataEntryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.AuthorizationDataEntry", false, AuthorizationDataEntryTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.AuthorizationDataEntry"), "name");
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
        check(methods.contains("toString"), "method:toString");
        check(methods.contains("writeEntry"), "method:writeEntry");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("adData"), "field:adData");
        check(fields.contains("adType"), "field:adType");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
