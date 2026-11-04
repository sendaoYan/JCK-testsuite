/*
 * @test
 * @summary jdk.internal.org.objectweb.asm.tree.analysis.Frame conformance: type shape via reflection (headless-safe).
 * @run main FrameTest
 */

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class FrameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasInterface(Class<?> c, String name) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true; return false; }
    static boolean hasMethod(Class<?> c, String name) { for (Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true; return false; }
    static boolean hasField(Class<?> c, String name) { for (Field f : c.getDeclaredFields()) if (f.getName().equals(name)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.org.objectweb.asm.tree.analysis.Frame", false, FrameTest.class.getClassLoader());
        check(c != null, "forName");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(Modifier.isPublic(c.getModifiers()) || Modifier.isProtected(c.getModifiers()), "public/protected");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass Object");
        check(hasMethod(c, "clearStack"), "declares clearStack");
        check(hasMethod(c, "execute"), "declares execute");
        check(hasMethod(c, "getLocal"), "declares getLocal");
        check(hasMethod(c, "getLocals"), "declares getLocals");
        check(hasMethod(c, "getMaxStackSize"), "declares getMaxStackSize");
        check(hasMethod(c, "getStack"), "declares getStack");
        check(hasMethod(c, "getStackSize"), "declares getStackSize");
        check(hasMethod(c, "init"), "declares init");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
