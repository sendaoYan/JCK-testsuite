/*
 * @test
 * @summary jdk.internal.foreign.StringSupport conformance: internal class reflection.
 * @modules java.base/jdk.internal.foreign:+open
 * @run main StringSupportTest
 */
public class StringSupportTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.foreign.StringSupport", false, StringSupportTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.foreign.StringSupport"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 24, "declared methods >= 24");
        check(hasM(ms, "bytesCompatible"), "method bytesCompatible");
        check(hasM(ms, "copyBytes"), "method copyBytes");
        check(hasM(ms, "copyToSegmentRaw"), "method copyToSegmentRaw");
        check(hasM(ms, "read"), "method read");
        check(hasM(ms, "readBytes"), "method readBytes");
        check(hasM(ms, "strlenByte"), "method strlenByte");
        check(hasM(ms, "strlenInt"), "method strlenInt");
        check(hasM(ms, "strlenShort"), "method strlenShort");
        check(hasM(ms, "write"), "method write");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 9, "declared fields >= 9");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
