/*
 * @test
 * @summary sun.nio.ch.Util conformance: internal package reflection.
 * @modules java.base/sun.nio.ch:+open
 * @run main UtilTest
 */
public class UtilTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true; return false; }
    static boolean hasField(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.getName().equals(n)) return true; return false; }
    static boolean hasInterface(Class<?> c, String n) { for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnumConstant(Class<?> c, String n) { for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.ch.Util", false, UtilTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        check(c.getInterfaces().length == 0, "interface count");
        check(hasMethod(c, "getTemporaryAlignedDirectBuffer"), "method getTemporaryAlignedDirectBuffer");
        check(hasMethod(c, "getTemporaryDirectBuffer"), "method getTemporaryDirectBuffer");
        check(hasMethod(c, "releaseTemporaryDirectBuffer"), "method releaseTemporaryDirectBuffer");
        check(hasField(c, "$assertionsDisabled"), "field $assertionsDisabled");
        check(hasField(c, "MAX_CACHED_BUFFER_SIZE"), "field MAX_CACHED_BUFFER_SIZE");
        check(hasField(c, "NIO_ACCESS"), "field NIO_ACCESS");
        check(hasField(c, "TEMP_BUF_POOL_SIZE"), "field TEMP_BUF_POOL_SIZE");
        check(hasField(c, "bufferCache"), "field bufferCache");
        check(hasField(c, "directByteBufferConstructor"), "field directByteBufferConstructor");
        check(hasField(c, "directByteBufferRConstructor"), "field directByteBufferRConstructor");
        check(hasField(c, "pageSize"), "field pageSize");
        check(hasField(c, "unsafe"), "field unsafe");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
