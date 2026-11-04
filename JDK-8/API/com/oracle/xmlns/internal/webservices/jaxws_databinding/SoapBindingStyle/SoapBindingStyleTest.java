/*
 * @test
 * @summary com.oracle.xmlns.internal.webservices.jaxws_databinding.SoapBindingStyle conformance: type shape via reflection (headless-safe).
 * @run main SoapBindingStyleTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class SoapBindingStyleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("com.oracle.xmlns.internal.webservices.jaxws_databinding.SoapBindingStyle", false, SoapBindingStyleTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == true, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "extends Enum");
        check(hasMethod(c, "fromValue"), "declares fromValue");
        check(hasMethod(c, "value"), "declares value");
        check(hasMethod(c, "valueOf"), "declares valueOf");
        check(hasMethod(c, "values"), "declares values");
        check(hasField(c, "DOCUMENT"), "declares field DOCUMENT");
        check(hasField(c, "RPC"), "declares field RPC");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
