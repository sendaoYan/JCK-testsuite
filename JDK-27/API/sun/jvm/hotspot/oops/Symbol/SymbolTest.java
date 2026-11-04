/*
 * @test
 * @summary sun.jvm.hotspot.oops.Symbol conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main SymbolTest
 */
import java.lang.reflect.*;

public class SymbolTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasField(Class<?> c, String n) {
        for (Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.Symbol", false, SymbolTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.VMObject".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "startsWith"), "method startsWith");
        check(hasMethod(c, "create"), "method create");
        check(hasMethod(c, "asByteArray"), "method asByteArray");
        check(hasMethod(c, "asString"), "method asString");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "getByteAt"), "method getByteAt");
        check(hasMethod(c, "isSymbol"), "method isSymbol");
        check(hasMethod(c, "fastCompare"), "method fastCompare");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
