/*
 * @test
 * @summary sun.reflect.ReflectionFactory conformance: reflection factory.
 * @run main ReflectionFactoryTest
 */

public class ReflectionFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) throws Exception {
        sun.reflect.ReflectionFactory rf = sun.reflect.ReflectionFactory.getReflectionFactory();
        check(rf != null, "getReflectionFactory");
        check(rf == sun.reflect.ReflectionFactory.getReflectionFactory(), "singleton");
        check(rf.newConstructorForSerialization(java.lang.String.class) != null, "newConstructorForSerialization");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
