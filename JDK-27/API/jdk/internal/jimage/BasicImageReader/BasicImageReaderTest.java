/*
 * @test
 * @summary jdk.internal.jimage.BasicImageReader conformance: internal package reflection.
 * @modules java.base/jdk.internal.jimage:+open
 * @run main BasicImageReaderTest
 */
public class BasicImageReaderTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.jimage.BasicImageReader", false, BasicImageReaderTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 1, "interface count");
        check(hasInterface(c, "java.lang.AutoCloseable"), "interface java.lang.AutoCloseable");
        check(hasMethod(c, "close"), "method close");
        check(hasMethod(c, "findLocation"), "method findLocation");
        check(hasMethod(c, "getAttributes"), "method getAttributes");
        check(hasMethod(c, "getByteOrder"), "method getByteOrder");
        check(hasMethod(c, "getEntryNames"), "method getEntryNames");
        check(hasMethod(c, "getHeader"), "method getHeader");
        check(hasMethod(c, "getImagePath"), "method getImagePath");
        check(hasMethod(c, "getLocationIndex"), "method getLocationIndex");
        check(hasMethod(c, "getName"), "method getName");
        check(hasMethod(c, "getResource"), "method getResource");
        check(hasMethod(c, "getResourceBuffer"), "method getResourceBuffer");
        check(hasMethod(c, "getResourceStream"), "method getResourceStream");
        check(hasMethod(c, "getString"), "method getString");
        check(hasMethod(c, "getStrings"), "method getStrings");
        check(hasMethod(c, "match"), "method match");
        check(hasMethod(c, "open"), "method open");
        check(hasMethod(c, "verifyLocation"), "method verifyLocation");
        check(hasField(c, "IS_64_BIT"), "field IS_64_BIT");
        check(hasField(c, "MAP_ALL"), "field MAP_ALL");
        check(hasField(c, "USE_JVM_MAP"), "field USE_JVM_MAP");
        check(hasField(c, "byteOrder"), "field byteOrder");
        check(hasField(c, "channel"), "field channel");
        check(hasField(c, "decompressor"), "field decompressor");
        check(hasField(c, "header"), "field header");
        check(hasField(c, "imagePath"), "field imagePath");
        check(hasField(c, "indexSize"), "field indexSize");
        check(hasField(c, "locations"), "field locations");
        check(hasField(c, "memoryMap"), "field memoryMap");
        check(hasField(c, "name"), "field name");
        check(hasField(c, "offsets"), "field offsets");
        check(hasField(c, "redirect"), "field redirect");
        check(hasField(c, "strings"), "field strings");
        check(hasField(c, "stringsReader"), "field stringsReader");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
