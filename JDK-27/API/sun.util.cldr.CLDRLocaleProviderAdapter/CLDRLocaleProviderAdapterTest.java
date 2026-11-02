/*
 * @test
 * @summary sun.util.cldr.CLDRLocaleProviderAdapter conformance: internal package reflection.
 * @modules java.base/sun.util.cldr:+open
 * @run main CLDRLocaleProviderAdapterTest
 */
public class CLDRLocaleProviderAdapterTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.cldr.CLDRLocaleProviderAdapter", false, CLDRLocaleProviderAdapterTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.provider.JRELocaleProviderAdapter"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "canonicalTZID"), "method canonicalTZID");
        check(hasMethod(c, "createLanguageTagSet"), "method createLanguageTagSet");
        check(hasMethod(c, "getAdapterType"), "method getAdapterType");
        check(hasMethod(c, "getAvailableLocales"), "method getAvailableLocales");
        check(hasMethod(c, "getBreakIteratorProvider"), "method getBreakIteratorProvider");
        check(hasMethod(c, "getCalendarDataProvider"), "method getCalendarDataProvider");
        check(hasMethod(c, "getCalendarNameProvider"), "method getCalendarNameProvider");
        check(hasMethod(c, "getCandidateLocales"), "method getCandidateLocales");
        check(hasMethod(c, "getCollatorProvider"), "method getCollatorProvider");
        check(hasMethod(c, "getTimeZoneNameProvider"), "method getTimeZoneNameProvider");
        check(hasMethod(c, "isSupportedProviderLocale"), "method isSupportedProviderLocale");
        check(hasField(c, "AVAILABLE_LOCALES"), "field AVAILABLE_LOCALES");
        check(hasField(c, "NB"), "field NB");
        check(hasField(c, "NO"), "field NO");
        check(hasField(c, "baseMetaInfo"), "field baseMetaInfo");
        check(hasField(c, "langAliasesCache"), "field langAliasesCache");
        check(hasField(c, "nonBaseMetaInfo"), "field nonBaseMetaInfo");
        check(hasField(c, "parentLocalesMap"), "field parentLocalesMap");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
