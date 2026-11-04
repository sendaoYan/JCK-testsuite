/*
 * @test
 * @summary sun.invoke.util.BytecodeName conformance: internal package reflection.
 * @modules java.base/sun.invoke.util:+open
 * @run main BytecodeNameTest
 */
public class BytecodeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.BytecodeName", false, BytecodeNameTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "isSafeBytecodeChar"), "method isSafeBytecodeChar");
        check(hasMethod(c, "isSafeBytecodeName"), "method isSafeBytecodeName");
        check(hasMethod(c, "parseBytecodeName"), "method parseBytecodeName");
        check(hasMethod(c, "toBytecodeName"), "method toBytecodeName");
        check(hasMethod(c, "toDisplayName"), "method toDisplayName");
        check(hasMethod(c, "toSourceName"), "method toSourceName");
        check(hasMethod(c, "unparseBytecodeName"), "method unparseBytecodeName");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "DANGEROUS_CHARS"), "field DANGEROUS_CHARS");
        check(hasField(c, "DANGEROUS_CHARS_A"), "field DANGEROUS_CHARS_A");
        check(hasField(c, "DANGEROUS_CHARS_CA"), "field DANGEROUS_CHARS_CA");
        check(hasField(c, "DANGEROUS_CHAR_FIRST_INDEX"), "field DANGEROUS_CHAR_FIRST_INDEX");
        check(hasField(c, "ESCAPE_C"), "field ESCAPE_C");
        check(hasField(c, "NULL_ESCAPE"), "field NULL_ESCAPE");
        check(hasField(c, "NULL_ESCAPE_C"), "field NULL_ESCAPE_C");
        check(hasField(c, "REPLACEMENT_CHARS"), "field REPLACEMENT_CHARS");
        check(hasField(c, "REPLACEMENT_CHARS_A"), "field REPLACEMENT_CHARS_A");
        check(hasField(c, "SPECIAL_BITMAP"), "field SPECIAL_BITMAP");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
