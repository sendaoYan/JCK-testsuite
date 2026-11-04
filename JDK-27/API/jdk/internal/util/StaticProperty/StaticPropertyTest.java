/*
 * @test
 * @summary jdk.internal.util.StaticProperty conformance: internal class reflection.
 * @modules java.base/jdk.internal.util:+open
 * @run main StaticPropertyTest
 */
public class StaticPropertyTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasM(java.lang.reflect.Method[] ms, String n) { for (java.lang.reflect.Method m : ms) if (m.getName().equals(n)) return true; return false; }
    static boolean hasF(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.getName().equals(n)) return true; return false; }
    static boolean hasI(Class<?>[] is, String n) { for (Class<?> i : is) if (i.getName().equals(n)) return true; return false; }
    static boolean hasEnum(java.lang.reflect.Field[] fs, String n) { for (java.lang.reflect.Field f : fs) if (f.isEnumConstant() && f.getName().equals(n)) return true; return false; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.util.StaticProperty", false, StaticPropertyTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.internal.util.StaticProperty"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "isAbstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == true, "isFinal");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.lang.reflect.Method[] ms = c.getDeclaredMethods();
        check(ms.length >= 21, "declared methods >= 21");
        check(hasM(ms, "fileEncoding"), "method fileEncoding");
        check(hasM(ms, "javaHome"), "method javaHome");
        check(hasM(ms, "javaIoTmpDir"), "method javaIoTmpDir");
        check(hasM(ms, "javaLibraryPath"), "method javaLibraryPath");
        check(hasM(ms, "javaPropertiesDate"), "method javaPropertiesDate");
        check(hasM(ms, "jdkSerialFilter"), "method jdkSerialFilter");
        check(hasM(ms, "jdkSerialFilterFactory"), "method jdkSerialFilterFactory");
        check(hasM(ms, "jnuEncoding"), "method jnuEncoding");
        check(hasM(ms, "nativeEncoding"), "method nativeEncoding");
        check(hasM(ms, "osArch"), "method osArch");
        check(hasM(ms, "osName"), "method osName");
        check(hasM(ms, "osVersion"), "method osVersion");
        check(hasM(ms, "stderrEncoding"), "method stderrEncoding");
        check(hasM(ms, "stdinEncoding"), "method stdinEncoding");
        check(hasM(ms, "stdoutEncoding"), "method stdoutEncoding");
        java.lang.reflect.Field[] fs = c.getDeclaredFields();
        check(fs.length >= 35, "declared fields >= 35");
        check(hasF(fs, "USER_COUNTRY"), "field USER_COUNTRY");
        check(hasF(fs, "USER_COUNTRY_DISPLAY"), "field USER_COUNTRY_DISPLAY");
        check(hasF(fs, "USER_COUNTRY_FORMAT"), "field USER_COUNTRY_FORMAT");
        check(hasF(fs, "USER_EXTENSIONS"), "field USER_EXTENSIONS");
        check(hasF(fs, "USER_EXTENSIONS_DISPLAY"), "field USER_EXTENSIONS_DISPLAY");
        check(hasF(fs, "USER_EXTENSIONS_FORMAT"), "field USER_EXTENSIONS_FORMAT");
        check(hasF(fs, "USER_LANGUAGE"), "field USER_LANGUAGE");
        check(hasF(fs, "USER_LANGUAGE_DISPLAY"), "field USER_LANGUAGE_DISPLAY");
        check(hasF(fs, "USER_LANGUAGE_FORMAT"), "field USER_LANGUAGE_FORMAT");
        check(hasF(fs, "USER_REGION"), "field USER_REGION");
        check(hasF(fs, "USER_SCRIPT"), "field USER_SCRIPT");
        check(hasF(fs, "USER_SCRIPT_DISPLAY"), "field USER_SCRIPT_DISPLAY");
        check(hasF(fs, "USER_SCRIPT_FORMAT"), "field USER_SCRIPT_FORMAT");
        check(hasF(fs, "USER_VARIANT"), "field USER_VARIANT");
        check(hasF(fs, "USER_VARIANT_DISPLAY"), "field USER_VARIANT_DISPLAY");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
