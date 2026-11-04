/*
 * @test
 * @summary sun.security.krb5.internal.crypto.Des conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.crypto:+open
 * @run main DesTest
 */
import java.lang.reflect.*;
import java.util.*;
public class DesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.crypto.Des", false, DesTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.crypto.Des"), "name");
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
        check(methods.contains("bad_key"), "method:bad_key");
        check(methods.contains("cbc_encrypt"), "method:cbc_encrypt");
        check(methods.contains("char_to_key"), "method:char_to_key");
        check(methods.contains("des_cksum"), "method:des_cksum");
        check(methods.contains("long2octet"), "method:long2octet");
        check(methods.contains("octet2long"), "method:octet2long");
        check(methods.contains("set_parity"), "method:set_parity");
        check(methods.contains("string_to_key_bytes"), "method:string_to_key_bytes");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
