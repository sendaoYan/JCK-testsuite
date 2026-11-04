/*
 * @test
 * @summary sun.util.locale.provider.LocaleProviderAdapter.Type conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main TypeTest
 */
public class TypeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.LocaleProviderAdapter$Type", false, TypeTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == true, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getAdapterClassName"), "method getAdapterClassName");
        check(hasMethod(c, "getTextResourcesPackage"), "method getTextResourcesPackage");
        check(hasMethod(c, "getUtilResourcesPackage"), "method getUtilResourcesPackage");
        check(hasMethod(c, "valueOf"), "method valueOf");
        check(hasMethod(c, "values"), "method values");
        check(hasField(c, "$VALUES"), "field $VALUES");
        check(hasField(c, "CLASSNAME"), "field CLASSNAME");
        check(hasField(c, "CLDR"), "field CLDR");
        check(hasField(c, "FALLBACK"), "field FALLBACK");
        check(hasField(c, "HOST"), "field HOST");
        check(hasField(c, "JRE"), "field JRE");
        check(hasField(c, "SPI"), "field SPI");
        check(hasField(c, "TEXT_RESOURCES_PACKAGE"), "field TEXT_RESOURCES_PACKAGE");
        check(hasField(c, "UTIL_RESOURCES_PACKAGE"), "field UTIL_RESOURCES_PACKAGE");
        check(hasEnumConstant(c, "CLDR"), "enum CLDR");
        check(hasEnumConstant(c, "FALLBACK"), "enum FALLBACK");
        check(hasEnumConstant(c, "HOST"), "enum HOST");
        check(hasEnumConstant(c, "JRE"), "enum JRE");
        check(hasEnumConstant(c, "SPI"), "enum SPI");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
