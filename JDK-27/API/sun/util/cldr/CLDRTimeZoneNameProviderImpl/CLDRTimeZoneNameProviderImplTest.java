/*
 * @test
 * @summary sun.util.cldr.CLDRTimeZoneNameProviderImpl conformance: internal package reflection.
 * @modules java.base/sun.util.cldr:+open
 * @run main CLDRTimeZoneNameProviderImplTest
 */
public class CLDRTimeZoneNameProviderImplTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.cldr.CLDRTimeZoneNameProviderImpl", false, CLDRTimeZoneNameProviderImplTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.TimeZoneNameProviderImpl"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getDisplayNameArray"), "method getDisplayNameArray");
        check(hasMethod(c, "getZoneStrings"), "method getZoneStrings");
        check(hasField(c, "INDEX_DST_LONG"), "field INDEX_DST_LONG");
        check(hasField(c, "INDEX_DST_SHORT"), "field INDEX_DST_SHORT");
        check(hasField(c, "INDEX_GEN_LONG"), "field INDEX_GEN_LONG");
        check(hasField(c, "INDEX_GEN_SHORT"), "field INDEX_GEN_SHORT");
        check(hasField(c, "INDEX_STD_LONG"), "field INDEX_STD_LONG");
        check(hasField(c, "INDEX_STD_SHORT"), "field INDEX_STD_SHORT");
        check(hasField(c, "INDEX_TZID"), "field INDEX_TZID");
        check(hasField(c, "NO_INHERITANCE_MARKER"), "field NO_INHERITANCE_MARKER");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
