/*
 * @test
 * @summary sun.util.locale.BaseLocale conformance: internal package reflection.
 * @modules java.base/sun.util.locale:+open
 * @run main BaseLocaleTest
 */
public class BaseLocaleTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.BaseLocale", false, BaseLocaleTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "convertOldISOCodes"), "method convertOldISOCodes");
        check(hasMethod(c, "equals"), "method equals");
        check(hasMethod(c, "getInstance"), "method getInstance");
        check(hasMethod(c, "getLanguage"), "method getLanguage");
        check(hasMethod(c, "getRegion"), "method getRegion");
        check(hasMethod(c, "getScript"), "method getScript");
        check(hasMethod(c, "getVariant"), "method getVariant");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "CACHE"), "field CACHE");
        check(hasField(c, "CANADA"), "field CANADA");
        check(hasField(c, "CANADA_FRENCH"), "field CANADA_FRENCH");
        check(hasField(c, "CHINESE"), "field CHINESE");
        check(hasField(c, "ENGLISH"), "field ENGLISH");
        check(hasField(c, "FRANCE"), "field FRANCE");
        check(hasField(c, "FRENCH"), "field FRENCH");
        check(hasField(c, "GERMAN"), "field GERMAN");
        check(hasField(c, "GERMANY"), "field GERMANY");
        check(hasField(c, "ITALIAN"), "field ITALIAN");
        check(hasField(c, "ITALY"), "field ITALY");
        check(hasField(c, "JAPAN"), "field JAPAN");
        check(hasField(c, "JAPANESE"), "field JAPANESE");
        check(hasField(c, "KOREA"), "field KOREA");
        check(hasField(c, "KOREAN"), "field KOREAN");
        check(hasField(c, "NUM_CONSTANTS"), "field NUM_CONSTANTS");
        check(hasField(c, "ROOT"), "field ROOT");
        check(hasField(c, "SEP"), "field SEP");
        check(hasField(c, "SIMPLIFIED_CHINESE"), "field SIMPLIFIED_CHINESE");
        check(hasField(c, "TRADITIONAL_CHINESE"), "field TRADITIONAL_CHINESE");
        check(hasField(c, "UK"), "field UK");
        check(hasField(c, "US"), "field US");
        check(hasField(c, "constantBaseLocales"), "field constantBaseLocales");
        check(hasField(c, "hash"), "field hash");
        check(hasField(c, "language"), "field language");
        check(hasField(c, "region"), "field region");
        check(hasField(c, "script"), "field script");
        check(hasField(c, "variant"), "field variant");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
