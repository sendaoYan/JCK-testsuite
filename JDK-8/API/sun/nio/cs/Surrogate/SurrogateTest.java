/*
 * @test
 * @summary sun.nio.cs.Surrogate conformance: class and members.
 * @run main SurrogateTest
 */

public class SurrogateTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("sun.nio.cs.Surrogate", false, ClassLoader.getSystemClassLoader());
        check(c.getName().equals("sun.nio.cs.Surrogate"), "binary name");
        check(!java.lang.reflect.Modifier.isPrivate(c.getModifiers()), "not private");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        java.util.Set<String> mnames = new java.util.HashSet<String>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) mnames.add(m.getName());
        check(mnames.contains("high"), "declares method high");
        check(mnames.contains("is"), "declares method is");
        check(mnames.contains("isHigh"), "declares method isHigh");
        check(mnames.contains("isLow"), "declares method isLow");
        check(mnames.contains("low"), "declares method low");
        check(mnames.contains("neededFor"), "declares method neededFor");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
