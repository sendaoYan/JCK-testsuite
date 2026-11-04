/*
 * @test
 * @summary jdk.internal.jimage.PreviewMode conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main PreviewModeTest
 */
public class PreviewModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.PreviewMode", false, PreviewModeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isPreviewModeEnabled"), "method isPreviewModeEnabled");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "DISABLED"), "field DISABLED");
        check(hasField(c, "ENABLED"), "field ENABLED");
        check(hasField(c, "FOR_RUNTIME"), "field FOR_RUNTIME");
        check(hasEnumConstant(c, "DISABLED"), "enum DISABLED");
        check(hasEnumConstant(c, "ENABLED"), "enum ENABLED");
        check(hasEnumConstant(c, "FOR_RUNTIME"), "enum FOR_RUNTIME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
