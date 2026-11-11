/*
 * @test
 * @requires os.family == "linux"
 * @summary jdk.internal.platform.CgroupSubsystemController conformance: internal package reflection.
 * @modules java.base/jdk.internal.platform:+open
 * @run main CgroupSubsystemControllerTest
 */
public class CgroupSubsystemControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.platform.CgroupSubsystemController", false, CgroupSubsystemControllerTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "convertStringToLong"), "method convertStringToLong");
        check(hasMethod(c, "getDoubleValue"), "method getDoubleValue");
        check(hasMethod(c, "getLongEntry"), "method getLongEntry");
        check(hasMethod(c, "getLongValue"), "method getLongValue");
        check(hasMethod(c, "getLongValueMatchingLine"), "method getLongValueMatchingLine");
        check(hasMethod(c, "getStringValue"), "method getStringValue");
        check(hasMethod(c, "path"), "method path");
        check(hasMethod(c, "stringRangeToIntArray"), "method stringRangeToIntArray");
        check(hasField(c, "EMPTY_STR"), "field EMPTY_STR");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
