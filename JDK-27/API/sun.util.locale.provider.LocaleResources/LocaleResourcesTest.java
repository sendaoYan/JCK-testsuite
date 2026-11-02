/*
 * @test
 * @summary sun.util.locale.provider.LocaleResources conformance: internal package reflection.
 * @modules java.base/sun.util.locale.provider:+open
 * @run main LocaleResourcesTest
 */
public class LocaleResourcesTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.provider.LocaleResources", false, LocaleResourcesTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getCNPatterns"), "method getCNPatterns");
        check(hasMethod(c, "getCalendarData"), "method getCalendarData");
        check(hasMethod(c, "getCollationData"), "method getCollationData");
        check(hasMethod(c, "getCurrencyName"), "method getCurrencyName");
        check(hasMethod(c, "getDateTimeFormatterPattern"), "method getDateTimeFormatterPattern");
        check(hasMethod(c, "getDateTimePattern"), "method getDateTimePattern");
        check(hasMethod(c, "getDecimalFormatSymbolsData"), "method getDecimalFormatSymbolsData");
        check(hasMethod(c, "getJavaTimeFormatData"), "method getJavaTimeFormatData");
        check(hasMethod(c, "getListPatterns"), "method getListPatterns");
        check(hasMethod(c, "getLocaleName"), "method getLocaleName");
        check(hasMethod(c, "getLocalizedPattern"), "method getLocalizedPattern");
        check(hasMethod(c, "getNumberPatterns"), "method getNumberPatterns");
        check(hasMethod(c, "getRules"), "method getRules");
        check(hasMethod(c, "getTimeZoneNames"), "method getTimeZoneNames");
        check(hasMethod(c, "trace"), "method trace");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "BREAK_ITERATOR_INFO"), "field BREAK_ITERATOR_INFO");
        check(hasField(c, "CALENDAR_DATA"), "field CALENDAR_DATA");
        check(hasField(c, "CALENDAR_NAMES"), "field CALENDAR_NAMES");
        check(hasField(c, "COLLATION_DATA"), "field COLLATION_DATA");
        check(hasField(c, "COMPACT_NUMBER_PATTERNS_CACHEKEY"), "field COMPACT_NUMBER_PATTERNS_CACHEKEY");
        check(hasField(c, "CPattern"), "field CPattern");
        check(hasField(c, "CURRENCY_NAMES"), "field CURRENCY_NAMES");
        check(hasField(c, "DATE_TIME_PATTERN"), "field DATE_TIME_PATTERN");
        check(hasField(c, "DECIMAL_FORMAT_SYMBOLS_DATA_CACHEKEY"), "field DECIMAL_FORMAT_SYMBOLS_DATA_CACHEKEY");
        check(hasField(c, "INPUT_SKELETONS"), "field INPUT_SKELETONS");
        check(hasField(c, "LIST_PATTERN"), "field LIST_PATTERN");
        check(hasField(c, "LOCALE_NAMES"), "field LOCALE_NAMES");
        check(hasField(c, "NULLOBJECT"), "field NULLOBJECT");
        check(hasField(c, "NUMBER_PATTERNS_CACHEKEY"), "field NUMBER_PATTERNS_CACHEKEY");
        check(hasField(c, "RULES_CACHEKEY"), "field RULES_CACHEKEY");
        check(hasField(c, "SKELETON_INPUT_REGIONS_KEY"), "field SKELETON_INPUT_REGIONS_KEY");
        check(hasField(c, "SKELETON_PATTERN"), "field SKELETON_PATTERN");
        check(hasField(c, "TIME_ZONE_NAMES"), "field TIME_ZONE_NAMES");
        check(hasField(c, "TRACE_ON"), "field TRACE_ON");
        check(hasField(c, "TZNB_EXCITY_PREFIX"), "field TZNB_EXCITY_PREFIX");
        check(hasField(c, "TZNB_METAZONE_DSTOFFSET_PREFIX"), "field TZNB_METAZONE_DSTOFFSET_PREFIX");
        check(hasField(c, "VALID_SKELETON_PATTERN"), "field VALID_SKELETON_PATTERN");
        check(hasField(c, "ZONE_IDS_CACHEKEY"), "field ZONE_IDS_CACHEKEY");
        check(hasField(c, "cache"), "field cache");
        check(hasField(c, "jPattern"), "field jPattern");
        check(hasField(c, "locale"), "field locale");
        check(hasField(c, "localeData"), "field localeData");
        check(hasField(c, "referenceQueue"), "field referenceQueue");
        check(hasField(c, "type"), "field type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
