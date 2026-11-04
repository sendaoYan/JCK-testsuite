/*
 * @test
 * @summary sun.jvm.hotspot.code.ObjectValue conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.code:+open
 * @run main ObjectValueTest
 */
import java.lang.reflect.*;

public class ObjectValueTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.code.ObjectValue", false, ObjectValueTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("sun.jvm.hotspot.code.ScopeValue".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "id"), "method id");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getFieldAt"), "method getFieldAt");
        check(hasMethod(c, "isObject"), "method isObject");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getKlass"), "method getKlass");
        check(hasMethod(c, "getFieldsValue"), "method getFieldsValue");
        check(hasMethod(c, "fieldsSize"), "method fieldsSize");
        check(hasField(c, "id"), "field id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
