/*
 * @test
 * @summary sun.util.locale.UnicodeLocaleExtension conformance: internal package reflection.
 * @modules java.base/sun.util.locale:+open
 * @run main UnicodeLocaleExtensionTest
 */
public class UnicodeLocaleExtensionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.locale.UnicodeLocaleExtension", false, UnicodeLocaleExtensionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("sun.util.locale.Extension"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getID"), "method getID");
        check(hasMethod(c, "getKey"), "method getKey");
        check(hasMethod(c, "getUnicodeLocaleAttributes"), "method getUnicodeLocaleAttributes");
        check(hasMethod(c, "getUnicodeLocaleKeys"), "method getUnicodeLocaleKeys");
        check(hasMethod(c, "getUnicodeLocaleType"), "method getUnicodeLocaleType");
        check(hasMethod(c, "getValue"), "method getValue");
        check(hasMethod(c, "isAttribute"), "method isAttribute");
        check(hasMethod(c, "isKey"), "method isKey");
        check(hasMethod(c, "isSingletonChar"), "method isSingletonChar");
        check(hasMethod(c, "isTypeSubtag"), "method isTypeSubtag");
        check(hasMethod(c, "toString"), "method toString");
        check(hasField(c, "CA_JAPANESE"), "field CA_JAPANESE");
        check(hasField(c, "NU_THAI"), "field NU_THAI");
        check(hasField(c, "SINGLETON"), "field SINGLETON");
        check(hasField(c, "attributes"), "field attributes");
        check(hasField(c, "keywords"), "field keywords");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
