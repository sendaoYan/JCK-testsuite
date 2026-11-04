/*
 * @test
 * @summary sun.util.locale.LocaleExtensions conformance: internal package reflection.
 * @modules java.base/sun.util.locale:+open
 * @run main LocaleExtensionsTest
 */
public class LocaleExtensionsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.LocaleExtensions", false, LocaleExtensionsTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getExtension"), "method getExtension");
        check(hasMethod(c, "getExtensionValue"), "method getExtensionValue");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "getKeys"), "method getKeys");
        check(hasMethod(c, "getUnicodeLocaleAttributes"), "method getUnicodeLocaleAttributes");
        check(hasMethod(c, "getUnicodeLocaleKeys"), "method getUnicodeLocaleKeys");
        check(hasMethod(c, "getUnicodeLocaleType"), "method getUnicodeLocaleType");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isEmpty"), "method isEmpty");
        check(hasMethod(c, "isValidKey"), "method isValidKey");
        check(hasMethod(c, "isValidUnicodeLocaleKey"), "method isValidUnicodeLocaleKey");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "CALENDAR_JAPANESE"), "field CALENDAR_JAPANESE");
        check(hasField(c, "NUMBER_THAI"), "field NUMBER_THAI");
        check(hasField(c, "extensionMap"), "field extensionMap");
        check(hasField(c, "id"), "field id");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
