/*
 * @test
 * @summary sun.util.locale.LanguageTag conformance: internal package reflection.
 * @modules java.base/sun.util.locale:+open
 * @run main LanguageTagTest
 */
public class LanguageTagTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.LanguageTag", false, LanguageTagTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Record"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canonicalizeExtension"), "method canonicalizeExtension");
        check(hasMethod(c, "canonicalizeExtensionSingleton"), "method canonicalizeExtensionSingleton");
        check(hasMethod(c, "canonicalizeExtensionSubtag"), "method canonicalizeExtensionSubtag");
        check(hasMethod(c, "canonicalizeExtlang"), "method canonicalizeExtlang");
        check(hasMethod(c, "canonicalizeLanguage"), "method canonicalizeLanguage");
        check(hasMethod(c, "canonicalizePrivateuse"), "method canonicalizePrivateuse");
        check(hasMethod(c, "canonicalizePrivateuseSubtag"), "method canonicalizePrivateuseSubtag");
        check(hasMethod(c, "canonicalizeRegion"), "method canonicalizeRegion");
        check(hasMethod(c, "canonicalizeScript"), "method canonicalizeScript");
        check(hasMethod(c, "canonicalizeVariant"), "method canonicalizeVariant");
        check(hasMethod(c, "caseFoldTag"), "method caseFoldTag");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "extensions"), "method extensions");
        check(hasMethod(c, "extlangs"), "method extlangs");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "isExtensionSingleton"), "method isExtensionSingleton");
        check(hasMethod(c, "isExtensionSingletonChar"), "method isExtensionSingletonChar");
        check(hasMethod(c, "isExtensionSubtag"), "method isExtensionSubtag");
        check(hasMethod(c, "isExtlang"), "method isExtlang");
        check(hasMethod(c, "isLanguage"), "method isLanguage");
        check(hasMethod(c, "isPrivateusePrefix"), "method isPrivateusePrefix");
        check(hasMethod(c, "isPrivateusePrefixChar"), "method isPrivateusePrefixChar");
        check(hasMethod(c, "isPrivateuseSubtag"), "method isPrivateuseSubtag");
        check(hasMethod(c, "isRegion"), "method isRegion");
        check(hasMethod(c, "isScript"), "method isScript");
        check(hasMethod(c, "isVariant"), "method isVariant");
        check(hasMethod(c, "language"), "method language");
        check(hasMethod(c, "parse"), "method parse");
        check(hasMethod(c, "parseLocale"), "method parseLocale");
        check(hasMethod(c, "privateuse"), "method privateuse");
        check(hasMethod(c, "region"), "method region");
        check(hasMethod(c, "script"), "method script");
        check(hasMethod(c, "toString"), "method toString");
        check(hasMethod(c, "variants"), "method variants");
        check(hasField(c, "EMPTY_SUBTAG"), "field EMPTY_SUBTAG");
        check(hasField(c, "EMPTY_SUBTAGS"), "field EMPTY_SUBTAGS");
        check(hasField(c, "PRIVATEUSE"), "field PRIVATEUSE");
        check(hasField(c, "PRIVUSE_VARIANT_PREFIX"), "field PRIVUSE_VARIANT_PREFIX");
        check(hasField(c, "SEP"), "field SEP");
        check(hasField(c, "UNDETERMINED"), "field UNDETERMINED");
        check(hasField(c, "extensions"), "field extensions");
        check(hasField(c, "extlangs"), "field extlangs");
        check(hasField(c, "language"), "field language");
        check(hasField(c, "privateuse"), "field privateuse");
        check(hasField(c, "region"), "field region");
        check(hasField(c, "script"), "field script");
        check(hasField(c, "variants"), "field variants");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
