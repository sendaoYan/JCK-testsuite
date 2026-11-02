/*
 * @test
 * @summary jdk.internal.module.ModuleInfo.Attributes conformance: internal package reflection.
 * @modules java.base/jdk.internal.module:+open
 * @run main AttributesTest
 */
public class AttributesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.module.ModuleInfo$Attributes", false, AttributesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "descriptor"), "method descriptor");
        check(hasMethod(c, "moduleResolution"), "method moduleResolution");
        check(hasMethod(c, "recordedHashes"), "method recordedHashes");
        check(hasMethod(c, "target"), "method target");
        check(hasField(c, "descriptor"), "field descriptor");
        check(hasField(c, "moduleResolution"), "field moduleResolution");
        check(hasField(c, "recordedHashes"), "field recordedHashes");
        check(hasField(c, "target"), "field target");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
