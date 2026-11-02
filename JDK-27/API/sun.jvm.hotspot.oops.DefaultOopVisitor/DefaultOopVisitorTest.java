/*
 * @test
 * @summary sun.jvm.hotspot.oops.DefaultOopVisitor conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main DefaultOopVisitorTest
 */
import java.lang.reflect.*;

public class DefaultOopVisitorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.DefaultOopVisitor", false, DefaultOopVisitorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "sun.jvm.hotspot.oops.OopVisitor"), "interface sun.jvm.hotspot.oops.OopVisitor");
        check(hasMethod(c, "doMetadata"), "method doMetadata");
        check(hasMethod(c, "doCInt"), "method doCInt");
        check(hasMethod(c, "setObj"), "method setObj");
        check(hasMethod(c, "prologue"), "method prologue");
        check(hasMethod(c, "epilogue"), "method epilogue");
        check(hasMethod(c, "doOop"), "method doOop");
        check(hasMethod(c, "doByte"), "method doByte");
        check(hasMethod(c, "doChar"), "method doChar");
        check(hasMethod(c, "doDouble"), "method doDouble");
        check(hasMethod(c, "doFloat"), "method doFloat");
        check(hasMethod(c, "doInt"), "method doInt");
        check(hasMethod(c, "doLong"), "method doLong");
        check(hasMethod(c, "doShort"), "method doShort");
        check(hasMethod(c, "doBoolean"), "method doBoolean");
        check(hasMethod(c, "getObj"), "method getObj");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
