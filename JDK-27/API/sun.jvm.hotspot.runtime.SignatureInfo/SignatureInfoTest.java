/*
 * @test
 * @summary sun.jvm.hotspot.runtime.SignatureInfo conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main SignatureInfoTest
 */
import java.lang.reflect.*;

public class SignatureInfoTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.SignatureInfo", false, SignatureInfoTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.runtime.SignatureIterator".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "size"), "method size");
        check(hasMethod(c, "type"), "method type");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "doByte"), "method doByte");
        check(hasMethod(c, "doChar"), "method doChar");
        check(hasMethod(c, "doDouble"), "method doDouble");
        check(hasMethod(c, "doFloat"), "method doFloat");
        check(hasMethod(c, "doInt"), "method doInt");
        check(hasMethod(c, "doLong"), "method doLong");
        check(hasMethod(c, "doShort"), "method doShort");
        check(hasMethod(c, "doArray"), "method doArray");
        check(hasMethod(c, "lazyIterate"), "method lazyIterate");
        check(hasMethod(c, "doBool"), "method doBool");
        check(hasMethod(c, "doVoid"), "method doVoid");
        check(hasMethod(c, "doObject"), "method doObject");
        check(hasField(c, "hasIterated"), "field hasIterated");
        check(hasField(c, "size"), "field size");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
