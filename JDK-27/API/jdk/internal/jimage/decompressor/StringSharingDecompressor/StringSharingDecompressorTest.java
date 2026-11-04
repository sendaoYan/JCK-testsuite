/*
 * @test
 * @summary jdk.internal.jimage.decompressor.StringSharingDecompressor conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage.decompressor:+open
 * @run main StringSharingDecompressorTest
 */
public class StringSharingDecompressorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.decompressor.StringSharingDecompressor", false, StringSharingDecompressorTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "jdk.internal.jimage.decompressor.ResourceDecompressor"), "interface jdk.internal.jimage.decompressor.ResourceDecompressor");
        check(hasMethod(c, "decompress"), "method decompress");
        check(hasMethod(c, "getEncoded"), "method getEncoded");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getSizes"), "method getSizes");
        check(hasMethod(c, "normalize"), "method normalize");
        check(hasField(c, "CONSTANT_Class"), "field CONSTANT_Class");
        check(hasField(c, "CONSTANT_Double"), "field CONSTANT_Double");
        check(hasField(c, "CONSTANT_Fieldref"), "field CONSTANT_Fieldref");
        check(hasField(c, "CONSTANT_Float"), "field CONSTANT_Float");
        check(hasField(c, "CONSTANT_Integer"), "field CONSTANT_Integer");
        check(hasField(c, "CONSTANT_InterfaceMethodref"), "field CONSTANT_InterfaceMethodref");
        check(hasField(c, "CONSTANT_InvokeDynamic"), "field CONSTANT_InvokeDynamic");
        check(hasField(c, "CONSTANT_Long"), "field CONSTANT_Long");
        check(hasField(c, "CONSTANT_MethodHandle"), "field CONSTANT_MethodHandle");
        check(hasField(c, "CONSTANT_MethodType"), "field CONSTANT_MethodType");
        check(hasField(c, "CONSTANT_Methodref"), "field CONSTANT_Methodref");
        check(hasField(c, "CONSTANT_Module"), "field CONSTANT_Module");
        check(hasField(c, "CONSTANT_NameAndType"), "field CONSTANT_NameAndType");
        check(hasField(c, "CONSTANT_Package"), "field CONSTANT_Package");
        check(hasField(c, "CONSTANT_String"), "field CONSTANT_String");
        check(hasField(c, "CONSTANT_Utf8"), "field CONSTANT_Utf8");
        check(hasField(c, "EXTERNALIZED_STRING"), "field EXTERNALIZED_STRING");
        check(hasField(c, "EXTERNALIZED_STRING_DESCRIPTOR"), "field EXTERNALIZED_STRING_DESCRIPTOR");
        check(hasField(c, "SIZES"), "field SIZES");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
