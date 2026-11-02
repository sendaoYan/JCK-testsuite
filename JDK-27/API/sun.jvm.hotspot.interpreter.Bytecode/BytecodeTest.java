/*
 * @test
 * @summary sun.jvm.hotspot.interpreter.Bytecode conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.interpreter:+open
 * @run main BytecodeTest
 */
import java.lang.reflect.*;

public class BytecodeTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.interpreter.Bytecode", false, BytecodeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "method"), "method method");
        check(hasMethod(c, "getLength"), "method getLength");
        check(hasMethod(c, "code"), "method code");
        check(hasMethod(c, "bci"), "method bci");
        check(hasMethod(c, "getIndexU1"), "method getIndexU1");
        check(hasMethod(c, "getIndexU2"), "method getIndexU2");
        check(hasMethod(c, "javaCode"), "method javaCode");
        check(hasMethod(c, "getJavaBytecodeName"), "method getJavaBytecodeName");
        check(hasMethod(c, "getBytecodeName"), "method getBytecodeName");
        check(hasMethod(c, "getIndexU4"), "method getIndexU4");
        check(hasMethod(c, "hasIndexU4"), "method hasIndexU4");
        check(hasMethod(c, "getIndexU1Cpcache"), "method getIndexU1Cpcache");
        check(hasMethod(c, "getIndexU2Cpcache"), "method getIndexU2Cpcache");
        check(hasMethod(c, "getJavaLength"), "method getJavaLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
