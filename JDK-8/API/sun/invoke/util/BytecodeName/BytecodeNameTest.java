/*
 * @test
 * @summary sun.invoke.util.BytecodeName conformance: class and members.
 * @run main BytecodeNameTest
 */

public class BytecodeNameTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.BytecodeName", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.invoke.util.BytecodeName"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("appendAll"), "declares method appendAll");
        check(mnames.contains("checkSafeBytecodeName"), "declares method checkSafeBytecodeName");
        check(mnames.contains("demangle"), "declares method demangle");
        check(mnames.contains("indexOfDangerousChar"), "declares method indexOfDangerousChar");
        check(mnames.contains("isDangerous"), "declares method isDangerous");
        check(mnames.contains("isJavaIdent"), "declares method isJavaIdent");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
