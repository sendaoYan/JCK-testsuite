/*
 * @test
 * @summary sun.invoke.util.BytecodeDescriptor conformance: class and members.
 * @run main BytecodeDescriptorTest
 */

public class BytecodeDescriptorTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.invoke.util.BytecodeDescriptor", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.invoke.util.BytecodeDescriptor"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("parseError"), "declares method parseError");
        check(mnames.contains("parseMethod"), "declares method parseMethod");
        check(mnames.contains("parseSig"), "declares method parseSig");
        check(mnames.contains("unparse"), "declares method unparse");
        check(mnames.contains("unparseMethod"), "declares method unparseMethod");
        check(mnames.contains("unparseSig"), "declares method unparseSig");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
