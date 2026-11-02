/*
 * @test
 * @summary jdk.internal.icu.text.BidiBase conformance: internal class reflection.
 * @modules java.base/jdk.internal.icu.text:+open
 * @run main BidiBaseTest
 */
public class BidiBaseTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.icu.text.BidiBase", false, BidiBaseTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.icu.text.BidiBase"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 80, "declared methods >= 80");
        check(hasM(ms, "baseIsLeftToRight"), "method baseIsLeftToRight");
        check(hasM(ms, "countRuns"), "method countRuns");
        check(hasM(ms, "getBaseLevel"), "method getBaseLevel");
        check(hasM(ms, "getCustomizedClass"), "method getCustomizedClass");
        check(hasM(ms, "getDirection"), "method getDirection");
        check(hasM(ms, "getLength"), "method getLength");
        check(hasM(ms, "getLevelAt"), "method getLevelAt");
        check(hasM(ms, "getParaLevel"), "method getParaLevel");
        check(hasM(ms, "getRunLevel"), "method getRunLevel");
        check(hasM(ms, "getRunLimit"), "method getRunLimit");
        check(hasM(ms, "getRunStart"), "method getRunStart");
        check(hasM(ms, "isInverse"), "method isInverse");
        check(hasM(ms, "isLeftToRight"), "method isLeftToRight");
        check(hasM(ms, "isMixed"), "method isMixed");
        check(hasM(ms, "isRightToLeft"), "method isRightToLeft");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 153, "declared fields >= 153");
        check(hasF(fs, "BIDI_PAIRED_BRACKET_TYPE"), "field BIDI_PAIRED_BRACKET_TYPE");
        check(hasF(fs, "DIRECTION_DEFAULT_RIGHT_TO_LEFT"), "field DIRECTION_DEFAULT_RIGHT_TO_LEFT");
        check(hasF(fs, "DO_MIRRORING"), "field DO_MIRRORING");
        check(hasF(fs, "INSERT_LRM_FOR_NUMERIC"), "field INSERT_LRM_FOR_NUMERIC");
        check(hasF(fs, "KEEP_BASE_COMBINING"), "field KEEP_BASE_COMBINING");
        check(hasF(fs, "LEVEL_DEFAULT_LTR"), "field LEVEL_DEFAULT_LTR");
        check(hasF(fs, "LEVEL_DEFAULT_RTL"), "field LEVEL_DEFAULT_RTL");
        check(hasF(fs, "LEVEL_OVERRIDE"), "field LEVEL_OVERRIDE");
        check(hasF(fs, "LTR"), "field LTR");
        check(hasF(fs, "MAP_NOWHERE"), "field MAP_NOWHERE");
        check(hasF(fs, "MAX_EXPLICIT_LEVEL"), "field MAX_EXPLICIT_LEVEL");
        check(hasF(fs, "MIXED"), "field MIXED");
        check(hasF(fs, "OUTPUT_REVERSE"), "field OUTPUT_REVERSE");
        check(hasF(fs, "REMOVE_BIDI_CONTROLS"), "field REMOVE_BIDI_CONTROLS");
        check(hasF(fs, "RTL"), "field RTL");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
