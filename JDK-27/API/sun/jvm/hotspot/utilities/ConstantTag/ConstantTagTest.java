/*
 * @test
 * @summary sun.jvm.hotspot.utilities.ConstantTag conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.utilities:+open
 * @run main ConstantTagTest
 */
import java.lang.reflect.*;

public class ConstantTagTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.utilities.ConstantTag", false, ConstantTagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "value"), "method value");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "basicType"), "method basicType");
        check(hasMethod(c, "isMethod"), "method isMethod");
        check(hasMethod(c, "isField"), "method isField");
        check(hasMethod(c, "isInvalid"), "method isInvalid");
        check(hasMethod(c, "isMethodType"), "method isMethodType");
        check(hasMethod(c, "isFloat"), "method isFloat");
        check(hasMethod(c, "isDouble"), "method isDouble");
        check(hasMethod(c, "isInt"), "method isInt");
        check(hasMethod(c, "isLong"), "method isLong");
        check(hasMethod(c, "isKlass"), "method isKlass");
        check(hasMethod(c, "isSymbol"), "method isSymbol");
        check(hasMethod(c, "isString"), "method isString");
        check(hasMethod(c, "isUnresolvedKlass"), "method isUnresolvedKlass");
        check(hasMethod(c, "isMethodHandle"), "method isMethodHandle");
        check(hasMethod(c, "isFieldOrMethod"), "method isFieldOrMethod");
        check(hasMethod(c, "isNameAndType"), "method isNameAndType");
        check(hasMethod(c, "isInvokeDynamic"), "method isInvokeDynamic");
        check(hasMethod(c, "isDynamicConstant"), "method isDynamicConstant");
        check(hasMethod(c, "isKlassIndex"), "method isKlassIndex");
        check(hasMethod(c, "isInterfaceMethod"), "method isInterfaceMethod");
        check(hasMethod(c, "isUtf8"), "method isUtf8");
        check(hasMethod(c, "isUnresolveKlassInError"), "method isUnresolveKlassInError");
        check(hasMethod(c, "isStringIndex"), "method isStringIndex");
        check(hasMethod(c, "isKlassReference"), "method isKlassReference");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
