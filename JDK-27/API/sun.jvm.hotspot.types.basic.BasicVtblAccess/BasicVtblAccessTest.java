/*
 * @test
 * @summary sun.jvm.hotspot.types.basic.BasicVtblAccess conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types.basic:+open
 * @run main BasicVtblAccessTest
 */
import java.lang.reflect.*;

public class BasicVtblAccessTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.basic.BasicVtblAccess", false, BasicVtblAccessTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.types.basic.VtblAccess"), "interface sun.jvm.hotspot.types.basic.VtblAccess");
        check(hasMethod(c, "vtblSymbolForType"), "method vtblSymbolForType");
        check(hasMethod(c, "getVtblForType"), "method getVtblForType");
        check(hasMethod(c, "clearCaches"), "method clearCaches");
        check(hasField(c, "symbolLookup"), "field symbolLookup");
        check(hasField(c, "dllNames"), "field dllNames");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
