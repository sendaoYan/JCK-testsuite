/*
 * @test
 * @summary sun.util.logging.PlatformLogger.Level conformance: enum constants and members.
 * @run main LevelTest
 */

public class LevelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.util.logging.PlatformLogger$Level", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.util.logging.PlatformLogger$Level"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isEnum(), "is enum");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Enum"), "superclass java.lang.Enum");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("intValue"), "declares method intValue");
        check(mnames.contains("valueOf"), "declares method valueOf");
        check(mnames.contains("values"), "declares method values");
        java.util.Set<String> consts = new java.util.HashSet<String>();
        for (java.lang.reflect.Field f : c.getDeclaredFields()) if (f.isEnumConstant()) consts.add(f.getName());
        check(consts.contains("ALL"), "enum constant ALL");
        check(consts.contains("FINEST"), "enum constant FINEST");
        check(consts.contains("FINER"), "enum constant FINER");
        check(consts.contains("FINE"), "enum constant FINE");
        check(consts.contains("CONFIG"), "enum constant CONFIG");
        check(consts.contains("INFO"), "enum constant INFO");
        check(consts.contains("WARNING"), "enum constant WARNING");
        check(consts.contains("SEVERE"), "enum constant SEVERE");
        check(consts.contains("OFF"), "enum constant OFF");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
