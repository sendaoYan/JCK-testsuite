/*
 * @test
 * @summary jdk.internal.access.SharedSecrets conformance: internal class reflection.
 * @modules java.base/jdk.internal.access:+open
 * @run main SharedSecretsTest
 */
public class SharedSecretsTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.internal.access.SharedSecrets", false, SharedSecretsTest.class.getClassLoader());
        check(c != null, "loads");
        check(!c.isInterface() && !c.isEnum(), "concrete class");
        boolean g = false, s = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
            if (m.getName().equals("getJavaUtilCollectionAccess")) g = true;
            if (m.getName().equals("setJavaUtilCollectionAccess")) s = true;
        }
        check(g, "getJavaUtilCollectionAccess");
        check(s, "setJavaUtilCollectionAccess");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
