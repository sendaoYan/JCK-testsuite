/*
 * @test
 * @summary sun.jvm.hotspot.interpreter.BytecodeTableswitch conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.interpreter:+open
 * @run main BytecodeTableswitchTest
 */
import java.lang.reflect.*;

public class BytecodeTableswitchTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.interpreter.BytecodeTableswitch", false, BytecodeTableswitchTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.interpreter.Bytecode".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "length"), "method length");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "at"), "method at");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "isValid"), "method isValid");
        check(hasMethod(c, "defaultOffset"), "method defaultOffset");
        check(hasMethod(c, "atCheck"), "method atCheck");
        check(hasMethod(c, "highKey"), "method highKey");
        check(hasMethod(c, "lowKey"), "method lowKey");
        check(hasMethod(c, "destOffsetAt"), "method destOffsetAt");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
