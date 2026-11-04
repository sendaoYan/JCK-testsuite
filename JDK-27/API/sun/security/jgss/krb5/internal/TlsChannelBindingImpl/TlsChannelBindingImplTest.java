/*
 * @test
 * @summary sun.security.jgss.krb5.internal.TlsChannelBindingImpl conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.jgss.krb5.internal:+open
 * @run main TlsChannelBindingImplTest
 */
import java.lang.reflect.*;
import java.util.*;
public class TlsChannelBindingImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.jgss.krb5.internal.TlsChannelBindingImpl", false, TlsChannelBindingImplTest.class.getClassLoader());
        check(c.getName().equals("sun.security.jgss.krb5.internal.TlsChannelBindingImpl"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("org.ietf.jgss.ChannelBinding"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
