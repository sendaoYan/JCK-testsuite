/*
 * @test
 * @summary jdk.internal.jimage.decompressor.CompressedResourceHeader conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage.decompressor:+open
 * @run main CompressedResourceHeaderTest
 */
public class CompressedResourceHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.decompressor.CompressedResourceHeader", false, CompressedResourceHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getBytes"), "method getBytes");
        check(hasMethod(c, "getDecompressorNameOffset"), "method getDecompressorNameOffset");
        check(hasMethod(c, "getResourceSize"), "method getResourceSize");
        check(hasMethod(c, "getSize"), "method getSize");
        check(hasMethod(c, "getUncompressedSize"), "method getUncompressedSize");
        check(hasMethod(c, "isTerminal"), "method isTerminal");
        check(hasMethod(c, "readFromResource"), "method readFromResource");
        check(hasField(c, "COMPRESSED_OFFSET"), "field COMPRESSED_OFFSET");
        check(hasField(c, "DECOMPRESSOR_NAME_OFFSET"), "field DECOMPRESSOR_NAME_OFFSET");
        check(hasField(c, "IS_TERMINAL_OFFSET"), "field IS_TERMINAL_OFFSET");
        check(hasField(c, "MAGIC"), "field MAGIC");
        check(hasField(c, "MAGIC_OFFSET"), "field MAGIC_OFFSET");
        check(hasField(c, "SIZE"), "field SIZE");
        check(hasField(c, "UNCOMPRESSED_OFFSET"), "field UNCOMPRESSED_OFFSET");
        check(hasField(c, "compressedSize"), "field compressedSize");
        check(hasField(c, "decompressorNameOffset"), "field decompressorNameOffset");
        check(hasField(c, "isTerminal"), "field isTerminal");
        check(hasField(c, "uncompressedSize"), "field uncompressedSize");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
