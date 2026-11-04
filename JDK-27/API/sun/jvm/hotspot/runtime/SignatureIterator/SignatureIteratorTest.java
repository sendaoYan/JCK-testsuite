/*
 * @test
 * @summary sun.jvm.hotspot.runtime.SignatureIterator conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.runtime:+open
 * @run main SignatureIteratorTest
 */
import java.lang.reflect.*;

public class SignatureIteratorTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.runtime.SignatureIterator", false, SignatureIteratorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "parseType"), "method parseType");
        check(hasMethod(c, "expect"), "method expect");
        check(hasMethod(c, "doByte"), "method doByte");
        check(hasMethod(c, "doChar"), "method doChar");
        check(hasMethod(c, "doDouble"), "method doDouble");
        check(hasMethod(c, "doFloat"), "method doFloat");
        check(hasMethod(c, "doInt"), "method doInt");
        check(hasMethod(c, "doLong"), "method doLong");
        check(hasMethod(c, "doShort"), "method doShort");
        check(hasMethod(c, "iterateParameters"), "method iterateParameters");
        check(hasMethod(c, "doArray"), "method doArray");
        check(hasMethod(c, "dispatchField"), "method dispatchField");
        check(hasMethod(c, "iterateReturntype"), "method iterateReturntype");
        check(hasMethod(c, "skipOptionalSize"), "method skipOptionalSize");
        check(hasMethod(c, "isReturnType"), "method isReturnType");
        check(hasMethod(c, "doBool"), "method doBool");
        check(hasMethod(c, "doVoid"), "method doVoid");
        check(hasMethod(c, "doObject"), "method doObject");
        check(hasMethod(c, "checkSignatureEnd"), "method checkSignatureEnd");
        check(hasMethod(c, "parameterIndex"), "method parameterIndex");
        check(hasField(c, "_signature"), "field _signature");
        check(hasField(c, "_index"), "field _index");
        check(hasField(c, "_parameter_index"), "field _parameter_index");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
