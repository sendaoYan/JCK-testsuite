/*
 * @test
 * @summary sun.nio.cs.GB18030 conformance: internal package reflection.
 * @modules java.base/sun.nio.cs:+open
 * @run main GB18030Test
 */
public class GB18030Test {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.GB18030", false, GB18030Test.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.nio.charset.Charset"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "contains"), "method contains");
        check(hasMethod(c, "newDecoder"), "method newDecoder");
        check(hasMethod(c, "newEncoder"), "method newEncoder");
        check(hasField(c, "GB18030_DOUBLE_BYTE"), "field GB18030_DOUBLE_BYTE");
        check(hasField(c, "GB18030_FOUR_BYTE"), "field GB18030_FOUR_BYTE");
        check(hasField(c, "GB18030_SINGLE_BYTE"), "field GB18030_SINGLE_BYTE");
        check(hasField(c, "IS_2000"), "field IS_2000");
        check(hasField(c, "decoderIndex1"), "field decoderIndex1");
        check(hasField(c, "decoderIndex2"), "field decoderIndex2");
        check(hasField(c, "encoderIndex1"), "field encoderIndex1");
        check(hasField(c, "encoderIndex2"), "field encoderIndex2");
        check(hasField(c, "index1"), "field index1");
        check(hasField(c, "index2"), "field index2");
        check(hasField(c, "innerDecoderIndex0"), "field innerDecoderIndex0");
        check(hasField(c, "innerDecoderIndex1"), "field innerDecoderIndex1");
        check(hasField(c, "innerDecoderIndex2"), "field innerDecoderIndex2");
        check(hasField(c, "innerDecoderIndex3"), "field innerDecoderIndex3");
        check(hasField(c, "innerDecoderIndex4"), "field innerDecoderIndex4");
        check(hasField(c, "innerDecoderIndex5"), "field innerDecoderIndex5");
        check(hasField(c, "innerEncoderIndex0"), "field innerEncoderIndex0");
        check(hasField(c, "innerEncoderIndex1"), "field innerEncoderIndex1");
        check(hasField(c, "innerEncoderIndex10"), "field innerEncoderIndex10");
        check(hasField(c, "innerEncoderIndex11"), "field innerEncoderIndex11");
        check(hasField(c, "innerEncoderIndex12"), "field innerEncoderIndex12");
        check(hasField(c, "innerEncoderIndex2"), "field innerEncoderIndex2");
        check(hasField(c, "innerEncoderIndex3"), "field innerEncoderIndex3");
        check(hasField(c, "innerEncoderIndex4"), "field innerEncoderIndex4");
        check(hasField(c, "innerEncoderIndex5"), "field innerEncoderIndex5");
        check(hasField(c, "innerEncoderIndex6"), "field innerEncoderIndex6");
        check(hasField(c, "innerEncoderIndex7"), "field innerEncoderIndex7");
        check(hasField(c, "innerEncoderIndex8"), "field innerEncoderIndex8");
        check(hasField(c, "innerEncoderIndex9"), "field innerEncoderIndex9");
        check(hasField(c, "innerIndex0"), "field innerIndex0");
        check(hasField(c, "innerIndex1"), "field innerIndex1");
        check(hasField(c, "innerIndex2"), "field innerIndex2");
        check(hasField(c, "innerIndex3"), "field innerIndex3");
        check(hasField(c, "innerIndex4"), "field innerIndex4");
        check(hasField(c, "innerIndex5"), "field innerIndex5");
        check(hasField(c, "innerIndex6"), "field innerIndex6");
        check(hasField(c, "innerIndex7"), "field innerIndex7");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
