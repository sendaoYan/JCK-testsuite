/*
 * @test
 * @summary sun.util.locale.InternalLocaleBuilder conformance: internal package reflection.
 * @modules java.base/sun.util.locale:+open
 * @run main InternalLocaleBuilderTest
 */
public class InternalLocaleBuilderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.InternalLocaleBuilder", false, InternalLocaleBuilderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "addUnicodeLocaleAttribute"), "method addUnicodeLocaleAttribute");
        check(hasMethod(c, "clear"), "method clear");
        check(hasMethod(c, "clearExtensions"), "method clearExtensions");
        check(hasMethod(c, "getBaseLocale"), "method getBaseLocale");
        check(hasMethod(c, "getLocaleExtensions"), "method getLocaleExtensions");
        check(hasMethod(c, "removeUnicodeLocaleAttribute"), "method removeUnicodeLocaleAttribute");
        check(hasMethod(c, "setExtension"), "method setExtension");
        check(hasMethod(c, "setExtensions"), "method setExtensions");
        check(hasMethod(c, "setLanguage"), "method setLanguage");
        check(hasMethod(c, "setLanguageTag"), "method setLanguageTag");
        check(hasMethod(c, "setLocale"), "method setLocale");
        check(hasMethod(c, "setRegion"), "method setRegion");
        check(hasMethod(c, "setScript"), "method setScript");
        check(hasMethod(c, "setUnicodeLocaleKeyword"), "method setUnicodeLocaleKeyword");
        check(hasMethod(c, "setVariant"), "method setVariant");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "PRIVATEUSE_KEY"), "field PRIVATEUSE_KEY");
        check(hasField(c, "extensions"), "field extensions");
        check(hasField(c, "language"), "field language");
        check(hasField(c, "region"), "field region");
        check(hasField(c, "script"), "field script");
        check(hasField(c, "uattributes"), "field uattributes");
        check(hasField(c, "ukeywords"), "field ukeywords");
        check(hasField(c, "variant"), "field variant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
