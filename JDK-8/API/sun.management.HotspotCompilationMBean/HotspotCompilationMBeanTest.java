/*
 * @test
 * @summary sun.management.HotspotCompilationMBean conformance: interface and members.
 * @run main HotspotCompilationMBeanTest
 */

public class HotspotCompilationMBeanTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.management.HotspotCompilationMBean", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.management.HotspotCompilationMBean"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.isInterface(), "is interface");
        check(c.getSuperclass() == null, "interface has no superclass");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("getBailoutCompileCount"), "declares method getBailoutCompileCount");
        check(mnames.contains("getCompiledMethodCodeSize"), "declares method getCompiledMethodCodeSize");
        check(mnames.contains("getCompiledMethodSize"), "declares method getCompiledMethodSize");
        check(mnames.contains("getCompilerThreadCount"), "declares method getCompilerThreadCount");
        check(mnames.contains("getCompilerThreadStats"), "declares method getCompilerThreadStats");
        check(mnames.contains("getFailedCompile"), "declares method getFailedCompile");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
