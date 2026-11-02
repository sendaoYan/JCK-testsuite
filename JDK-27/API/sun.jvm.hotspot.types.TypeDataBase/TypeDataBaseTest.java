/*
 * @test
 * @summary sun.jvm.hotspot.types.TypeDataBase conformance: internal package reflection.
 * @modules jdk.hotspot.agent/sun.jvm.hotspot.types:+open
 * @run main TypeDataBaseTest
 */
import java.lang.reflect.*;

public class TypeDataBaseTest {
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
        Class<?> c = Class.forName("sun.jvm.hotspot.types.TypeDataBase", false, TypeDataBaseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "interface");
        check(c.isEnum() == false, "enum");
        check(c.isAnnotation() == false, "annotation");
        check(Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getJBooleanType"), "method getJBooleanType");
        check(hasMethod(c, "getJByteType"), "method getJByteType");
        check(hasMethod(c, "getJCharType"), "method getJCharType");
        check(hasMethod(c, "getJDoubleType"), "method getJDoubleType");
        check(hasMethod(c, "getJFloatType"), "method getJFloatType");
        check(hasMethod(c, "getJIntType"), "method getJIntType");
        check(hasMethod(c, "getJLongType"), "method getJLongType");
        check(hasMethod(c, "getJShortType"), "method getJShortType");
        check(hasMethod(c, "lookupType"), "method lookupType");
        check(hasMethod(c, "lookupIntConstant"), "method lookupIntConstant");
        check(hasMethod(c, "getAddressSize"), "method getAddressSize");
        check(hasMethod(c, "lookupLongConstant"), "method lookupLongConstant");
        check(hasMethod(c, "getOopSize"), "method getOopSize");
        check(hasMethod(c, "addressTypeIsEqualToType"), "method addressTypeIsEqualToType");
        check(hasMethod(c, "guessTypeForAddress"), "method guessTypeForAddress");
        check(hasMethod(c, "findDynamicTypeForAddress"), "method findDynamicTypeForAddress");
        check(hasMethod(c, "getTypes"), "method getTypes");
        check(hasMethod(c, "getIntConstants"), "method getIntConstants");
        check(hasMethod(c, "getLongConstants"), "method getLongConstants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
