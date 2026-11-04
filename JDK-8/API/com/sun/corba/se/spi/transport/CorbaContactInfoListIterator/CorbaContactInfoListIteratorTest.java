/*
 * @test
 * @summary com.sun.corba.se.spi.transport.CorbaContactInfoListIterator conformance: type shape via reflection (headless-safe).
 * @run main CorbaContactInfoListIteratorTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;

public class CorbaContactInfoListIteratorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.corba.se.spi.transport.CorbaContactInfoListIterator", false, CorbaContactInfoListIteratorTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "interface superclass null");
        Set<String> in = new HashSet<String>();
        for (Class<?> i : c.getInterfaces()) in.add(i.getName());
        check(in.contains("com.sun.corba.se.pept.transport.ContactInfoListIterator"), "interface com.sun.corba.se.pept.transport.ContactInfoListIterator");
        Set<String> mn = new HashSet<String>();
        for (Method m : c.getDeclaredMethods()) if (Modifier.isPublic(m.getModifiers())) mn.add(m.getName());
        check(mn.size() == 2, "public declared method count");
        check(mn.contains("reportAddrDispositionRetry"), "method reportAddrDispositionRetry");
        check(mn.contains("reportRedirect"), "method reportRedirect");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
