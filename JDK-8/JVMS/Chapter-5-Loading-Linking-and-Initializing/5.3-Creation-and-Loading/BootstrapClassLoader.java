/*
 * @test
 * @summary JVMS 5.3.1: classes loaded by the bootstrap class loader are
 *          reported with a null class loader, including core platform classes
 *          and primitive types.
 * @run main BootstrapClassLoader
 */

public class BootstrapClassLoader {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        check(Object.class.getClassLoader() == null,
                "Object is loaded by the bootstrap (null) loader");
        check(String.class.getClassLoader() == null,
                "String is loaded by the bootstrap (null) loader");
        check(java.util.List.class.getClassLoader() == null,
                "java.util.List is loaded by the bootstrap (null) loader");
        check(int.class.getClassLoader() == null,
                "a primitive type has a null class loader");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
