/*
 * @test
 * @summary jdk.internal.jimage.ImageStringsReader conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ImageStringsReaderTest
 */
public class ImageStringsReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageStringsReader", false, ImageStringsReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "jdk.internal.jimage.ImageStrings"), "interface jdk.internal.jimage.ImageStrings");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "get"), "method get");
        check(hasMethod(c, "hashCode"), "method hashCode");
        check(hasMethod(c, "match"), "method match");
        check(hasMethod(c, "mutf8FromString"), "method mutf8FromString");
        check(hasMethod(c, "stringFromByteBuffer"), "method stringFromByteBuffer");
        check(hasMethod(c, "stringFromMUTF8"), "method stringFromMUTF8");
        check(hasMethod(c, "unmaskedHashCode"), "method unmaskedHashCode");
        check(hasField(c, "HASH_MULTIPLIER"), "field HASH_MULTIPLIER");
        check(hasField(c, "POSITIVE_MASK"), "field POSITIVE_MASK");
        check(hasField(c, "reader"), "field reader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
