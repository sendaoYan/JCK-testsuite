/*
 * @test
 * @summary jdk.internal.lang.CaseFolding conformance: internal package reflection.
 * @modules java.base/jdk.internal.lang:+open
 * @run main CaseFoldingTest
 */
public class CaseFoldingTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.lang.CaseFolding", false, CaseFoldingTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "fold"), "method fold");
        check(hasMethod(c, "getClassRangeClosingCharacters"), "method getClassRangeClosingCharacters");
        check(hasMethod(c, "isDefined"), "method isDefined");
        check(hasMethod(c, "isSingleCodePoint"), "method isSingleCodePoint");
        check(hasField(c, "CASE_FOLDING_CPS"), "field CASE_FOLDING_CPS");
        check(hasField(c, "CASE_FOLDING_HASHES"), "field CASE_FOLDING_HASHES");
        check(hasField(c, "CASE_FOLDING_VALUES"), "field CASE_FOLDING_VALUES");
        check(hasField(c, "HASH_CP"), "field HASH_CP");
        check(hasField(c, "HASH_INDEX"), "field HASH_INDEX");
        check(hasField(c, "HASH_NEXT"), "field HASH_NEXT");
        check(hasField(c, "expanded_case_cps"), "field expanded_case_cps");
        check(hasField(c, "expanded_case_map"), "field expanded_case_map");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
