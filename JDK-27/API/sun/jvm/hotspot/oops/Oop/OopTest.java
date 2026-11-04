/*
 * @test
 * @summary sun.jvm.hotspot.oops.Oop conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.oops:+open
 * @run main OopTest
 */
import java.lang.reflect.*;

public class OopTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.oops.Oop", false, OopTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check("java.lang.Object".equals(c.getSuperclass().getName()), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isInstance"), "method isInstance");
        check(hasMethod(c, "isArray"), "method isArray");
        check(hasMethod(c, "print"), "method print");
        check(hasMethod(c, "getHandle"), "method getHandle");
        check(hasMethod(c, "iterate"), "method iterate");
        check(hasMethod(c, "verify"), "method verify");
        check(hasMethod(c, "getHeaderSize"), "method getHeaderSize");
        check(hasMethod(c, "getMark"), "method getMark");
        check(hasMethod(c, "getObjectSize"), "method getObjectSize");
        check(hasMethod(c, "printValueOn"), "method printValueOn");
        check(hasMethod(c, "printOopValueOn"), "method printOopValueOn");
        check(hasMethod(c, "printOn"), "method printOn");
        check(hasMethod(c, "getKlass"), "method getKlass");
        check(hasMethod(c, "getKlassForOopHandle"), "method getKlassForOopHandle");
        check(hasMethod(c, "printValue"), "method printValue");
        check(hasMethod(c, "slowIdentityHash"), "method slowIdentityHash");
        check(hasMethod(c, "printRawOn"), "method printRawOn");
        check(hasMethod(c, "isA"), "method isA");
        check(hasMethod(c, "isInstanceRef"), "method isInstanceRef");
        check(hasMethod(c, "isObjArray"), "method isObjArray");
        check(hasMethod(c, "isTypeArray"), "method isTypeArray");
        check(hasMethod(c, "isThread"), "method isThread");
        check(hasMethod(c, "alignObjectSize"), "method alignObjectSize");
        check(hasMethod(c, "alignObjectOffset"), "method alignObjectOffset");
        check(hasMethod(c, "identityHash"), "method identityHash");
        check(hasMethod(c, "printRaw"), "method printRaw");
        check(hasMethod(c, "printOopAddressOn"), "method printOopAddressOn");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
