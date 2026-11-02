/*
 * @test
 * @summary com.sun.org.apache.xml.internal.security.utils.I18n conformance: internal package reflection.
 * @modules java.xml.crypto/com.sun.org.apache.xml.internal.security.utils:+open
 * @run main I18nTest
 */
import java.lang.reflect.*;
import java.util.*;
public class I18nTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.sun.org.apache.xml.internal.security.utils.I18n", false, I18nTest.class.getClassLoader());
        check(c.getName().equals("com.sun.org.apache.xml.internal.security.utils.I18n"), "name");
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
        check(methods.contains("getExceptionMessage"), "method:getExceptionMessage");
        check(methods.contains("init"), "method:init");
        check(methods.contains("translate"), "method:translate");
        Set<String> fields = new HashSet<>();
        for (Field f : c.getDeclaredFields()) {
            int mod = f.getModifiers();
            if (Modifier.isPublic(mod) || Modifier.isProtected(mod)) fields.add(f.getName());
        }
        check(fields.contains("NOT_INITIALIZED_MSG"), "field:NOT_INITIALIZED_MSG");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
