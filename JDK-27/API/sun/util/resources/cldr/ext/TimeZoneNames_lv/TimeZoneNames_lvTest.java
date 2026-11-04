/*
 * @test
 * @summary sun.util.resources.cldr.ext.TimeZoneNames_lv conformance: internal package reflection.
 * @modules jdk.localedata/sun.util.resources.cldr.ext:+open
 * @run main TimeZoneNames_lvTest
 */
public class TimeZoneNames_lvTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name, boolean stat) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods())
            if (m.getName().equals(name) && java.lang.reflect.Modifier.isStatic(m.getModifiers()) == stat) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.resources.cldr.ext.TimeZoneNames_lv", false, TimeZoneNames_lvTest.class.getClassLoader());
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum() && !c.isAnnotation(), "concrete class");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "non-abstract");
        check("sun.util.resources.TimeZoneNamesBundle".equals(c.getSuperclass().getName()), "superclass sun.util.resources.TimeZoneNamesBundle");
        check(hasMethod(c, "getContents", false), "method getContents");
        java.lang.reflect.Constructor<?> ctor = c.getDeclaredConstructor();
        check(java.lang.reflect.Modifier.isPublic(ctor.getModifiers()), "public no-arg ctor");
        Object inst = ctor.newInstance();
        java.lang.reflect.Method gc = c.getDeclaredMethod("getContents");
        gc.setAccessible(true);
        Object[][] contents = (Object[][]) gc.invoke(inst);
        check(contents != null, "non-null getContents");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
