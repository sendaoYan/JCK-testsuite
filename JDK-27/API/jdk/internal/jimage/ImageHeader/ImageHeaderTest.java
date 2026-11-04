/*
 * @test
 * @summary jdk.internal.jimage.ImageHeader conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main ImageHeaderTest
 */
public class ImageHeaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.ImageHeader", false, ImageHeaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getFlags"), "method getFlags");
        check(hasMethod(c, "getHeaderSize"), "method getHeaderSize");
        check(hasMethod(c, "getIndexSize"), "method getIndexSize");
        check(hasMethod(c, "getLocationsSize"), "method getLocationsSize");
        check(hasMethod(c, "getMagic"), "method getMagic");
        check(hasMethod(c, "getMajorVersion"), "method getMajorVersion");
        check(hasMethod(c, "getMinorVersion"), "method getMinorVersion");
        check(hasMethod(c, "getOffsetsSize"), "method getOffsetsSize");
        check(hasMethod(c, "getRedirectSize"), "method getRedirectSize");
        check(hasMethod(c, "getResourceCount"), "method getResourceCount");
        check(hasMethod(c, "getStringsSize"), "method getStringsSize");
        check(hasMethod(c, "getTableLength"), "method getTableLength");
        check(hasMethod(c, "writeTo"), "method writeTo");
        check(hasField(c, "HEADER_SLOTS"), "field HEADER_SLOTS");
        check(hasField(c, "MAGIC"), "field MAGIC");
        check(hasField(c, "MAJOR_VERSION"), "field MAJOR_VERSION");
        check(hasField(c, "MINOR_VERSION"), "field MINOR_VERSION");
        check(hasField(c, "flags"), "field flags");
        check(hasField(c, "locationsSize"), "field locationsSize");
        check(hasField(c, "magic"), "field magic");
        check(hasField(c, "majorVersion"), "field majorVersion");
        check(hasField(c, "minorVersion"), "field minorVersion");
        check(hasField(c, "resourceCount"), "field resourceCount");
        check(hasField(c, "stringsSize"), "field stringsSize");
        check(hasField(c, "tableLength"), "field tableLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
