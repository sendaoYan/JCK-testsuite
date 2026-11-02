/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.c14n.implementations.UtfHelpper conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.c14n.implementations:+open
 * @run main UtfHelpperTest
 */
import java.lang.reflect.*;
import java.util.*;
public class UtfHelpperTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.c14n.implementations.UtfHelpper", false, UtfHelpperTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.c14n.implementations.UtfHelpper"), "name");
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
        check(methods.contains("getStringInUtf8"), "method:getStringInUtf8");
        check(methods.contains("writeByte"), "method:writeByte");
        check(methods.contains("writeCodePointToUtf8"), "method:writeCodePointToUtf8");
        check(methods.contains("writeStringToUtf8"), "method:writeStringToUtf8");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
