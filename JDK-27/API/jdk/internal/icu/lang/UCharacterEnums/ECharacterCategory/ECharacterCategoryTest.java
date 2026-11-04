/*
 * @test
 * @summary jdk.internal.icu.lang.UCharacterEnums.ECharacterCategory conformance: internal interface reflection.
 * @modules java.base/jdk.internal.icu.lang:+open
 * @run main ECharacterCategoryTest
 */
public class ECharacterCategoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.lang.UCharacterEnums$ECharacterCategory", false, ECharacterCategoryTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.lang.UCharacterEnums$ECharacterCategory"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 34, "declared fields >= 34");
        check(hasF(fs, "CHAR_CATEGORY_COUNT"), "field CHAR_CATEGORY_COUNT");
        check(hasF(fs, "COMBINING_SPACING_MARK"), "field COMBINING_SPACING_MARK");
        check(hasF(fs, "CONNECTOR_PUNCTUATION"), "field CONNECTOR_PUNCTUATION");
        check(hasF(fs, "CONTROL"), "field CONTROL");
        check(hasF(fs, "CURRENCY_SYMBOL"), "field CURRENCY_SYMBOL");
        check(hasF(fs, "DASH_PUNCTUATION"), "field DASH_PUNCTUATION");
        check(hasF(fs, "DECIMAL_DIGIT_NUMBER"), "field DECIMAL_DIGIT_NUMBER");
        check(hasF(fs, "ENCLOSING_MARK"), "field ENCLOSING_MARK");
        check(hasF(fs, "END_PUNCTUATION"), "field END_PUNCTUATION");
        check(hasF(fs, "FINAL_PUNCTUATION"), "field FINAL_PUNCTUATION");
        check(hasF(fs, "FINAL_QUOTE_PUNCTUATION"), "field FINAL_QUOTE_PUNCTUATION");
        check(hasF(fs, "FORMAT"), "field FORMAT");
        check(hasF(fs, "GENERAL_OTHER_TYPES"), "field GENERAL_OTHER_TYPES");
        check(hasF(fs, "INITIAL_PUNCTUATION"), "field INITIAL_PUNCTUATION");
        check(hasF(fs, "INITIAL_QUOTE_PUNCTUATION"), "field INITIAL_QUOTE_PUNCTUATION");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
