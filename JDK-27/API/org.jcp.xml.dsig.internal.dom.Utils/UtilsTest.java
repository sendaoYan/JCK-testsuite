/*
 * @test
 * @summary org.jcp.xml.dsig.internal.dom.Utils conformance: internal package reflection.
 * @modules java.xml.crypto/org.jcp.xml.dsig.internal.dom:+open
 * @run main UtilsTest
 */
import java.lang.reflect.*;
import java.util.*;
public class UtilsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("org.jcp.xml.dsig.internal.dom.Utils", false, UtilsTest.class.getClassLoader());
        check(c.getName().equals("org.jcp.xml.dsig.internal.dom.Utils"), "name");
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
        check(methods.contains("parseIdFromSameDocumentURI"), "method:parseIdFromSameDocumentURI");
        check(methods.contains("readBytesFromStream"), "method:readBytesFromStream");
        check(methods.contains("sameDocumentURI"), "method:sameDocumentURI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
