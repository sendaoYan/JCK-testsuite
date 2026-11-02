/*
 * @test
 * @summary sun.jvm.hotspot.runtime.SignatureConverter conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main SignatureConverterTest
 */
import java.lang.reflect.*;

public class SignatureConverterTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.SignatureConverter", false, SignatureConverterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.SignatureIterator".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "doByte"), "method doByte");
        check(hasMethod(c, "doChar"), "method doChar");
        check(hasMethod(c, "doDouble"), "method doDouble");
        check(hasMethod(c, "doFloat"), "method doFloat");
        check(hasMethod(c, "doInt"), "method doInt");
        check(hasMethod(c, "doLong"), "method doLong");
        check(hasMethod(c, "doShort"), "method doShort");
        check(hasMethod(c, "doArray"), "method doArray");
        check(hasMethod(c, "doBool"), "method doBool");
        check(hasMethod(c, "doVoid"), "method doVoid");
        check(hasMethod(c, "doObject"), "method doObject");
        check(hasMethod(c, "appendComma"), "method appendComma");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
