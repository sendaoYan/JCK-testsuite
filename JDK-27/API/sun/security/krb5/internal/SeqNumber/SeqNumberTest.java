/*
 * @test
 * @summary sun.security.krb5.internal.SeqNumber conformance: internal package reflection.
 * @modules java.security.jgss/sun.security.krb5.internal:+open
 * @run main SeqNumberTest
 */
import java.lang.reflect.*;
import java.util.*;
public class SeqNumberTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.security.krb5.internal.SeqNumber", false, SeqNumberTest.class.getClassLoader());
        check(c.getName().equals("sun.security.krb5.internal.SeqNumber"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass-null");
        check(c.getInterfaces().length == 0, "no-interfaces");
        Set<String> methods = new HashSet<>();
        for (Method m : c.getDeclaredMethods()) {
            int mod = m.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) methods.add(m.getName());
        }
        check(methods.contains("current"), "method:current");
        check(methods.contains("init"), "method:init");
        check(methods.contains("next"), "method:next");
        check(methods.contains("randInit"), "method:randInit");
        check(methods.contains("step"), "method:step");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
