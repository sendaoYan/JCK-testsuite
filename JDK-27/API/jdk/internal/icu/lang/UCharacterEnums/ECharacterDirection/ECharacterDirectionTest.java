/*
 * @test
 * @summary jdk.internal.icu.lang.UCharacterEnums.ECharacterDirection conformance: internal interface reflection.
 * @modules java.base/jdk.internal.icu.lang:+open
 * @run main ECharacterDirectionTest
 */
public class ECharacterDirectionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.lang.UCharacterEnums$ECharacterDirection", false, ECharacterDirectionTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.lang.UCharacterEnums$ECharacterDirection"), "name");
        check(c.isInterface() == true, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == true, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() == null, "superclass null");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 40, "declared fields >= 40");
        check(hasF(fs, "ARABIC_NUMBER"), "field ARABIC_NUMBER");
        check(hasF(fs, "BLOCK_SEPARATOR"), "field BLOCK_SEPARATOR");
        check(hasF(fs, "BOUNDARY_NEUTRAL"), "field BOUNDARY_NEUTRAL");
        check(hasF(fs, "CHAR_DIRECTION_COUNT"), "field CHAR_DIRECTION_COUNT");
        check(hasF(fs, "COMMON_NUMBER_SEPARATOR"), "field COMMON_NUMBER_SEPARATOR");
        check(hasF(fs, "DIRECTIONALITY_ARABIC_NUMBER"), "field DIRECTIONALITY_ARABIC_NUMBER");
        check(hasF(fs, "DIRECTIONALITY_BOUNDARY_NEUTRAL"), "field DIRECTIONALITY_BOUNDARY_NEUTRAL");
        check(hasF(fs, "DIRECTIONALITY_COMMON_NUMBER_SEPARATOR"), "field DIRECTIONALITY_COMMON_NUMBER_SEPARATOR");
        check(hasF(fs, "DIRECTIONALITY_EUROPEAN_NUMBER"), "field DIRECTIONALITY_EUROPEAN_NUMBER");
        check(hasF(fs, "DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR"), "field DIRECTIONALITY_EUROPEAN_NUMBER_SEPARATOR");
        check(hasF(fs, "DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR"), "field DIRECTIONALITY_EUROPEAN_NUMBER_TERMINATOR");
        check(hasF(fs, "DIRECTIONALITY_LEFT_TO_RIGHT"), "field DIRECTIONALITY_LEFT_TO_RIGHT");
        check(hasF(fs, "DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING"), "field DIRECTIONALITY_LEFT_TO_RIGHT_EMBEDDING");
        check(hasF(fs, "DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE"), "field DIRECTIONALITY_LEFT_TO_RIGHT_OVERRIDE");
        check(hasF(fs, "DIRECTIONALITY_NON_SPACING_MARK"), "field DIRECTIONALITY_NON_SPACING_MARK");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
