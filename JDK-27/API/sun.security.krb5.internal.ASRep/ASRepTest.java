/*
 * @test
 * @summary sun.security.krb5.internal.ASRep conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main ASRepTest
 */
import java.lang.reflect.*;
import java.util.*;
public class ASRepTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.ASRep", false, ASRepTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.ASRep"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass().getName().equals("sun.security.krb5.internal.KDCRep"), "superclass");
        check(c.getInterfaces().length == 0, "no-interfaces");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
