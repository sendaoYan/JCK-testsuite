/*
 * @test
 * @summary sun.security.krb5.internal.util.KrbDataInputStream conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal.util:+open
 * @run main KrbDataInputStreamTest
 */
import java.lang.reflect.*;
import java.util.*;
public class KrbDataInputStreamTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.util.KrbDataInputStream", false, KrbDataInputStreamTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.util.KrbDataInputStream"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("java.io.BufferedInputStream"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("read"), "method:read");
        check(methods.contains("readLength4"), "method:readLength4");
        check(methods.contains("readVersion"), "method:readVersion");
        check(methods.contains("setNativeByteOrder"), "method:setNativeByteOrder");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
