/*
 * @test
 * @summary JVMS 5.3.3: an array class is created on demand and takes the class
 *          loader of its component type (null for primitive and bootstrap
 *          component types); its component type is recoverable via reflection.
 * @run main ArrayClassLoader
 */

public class ArrayClassLoader {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(int[].class.getClassLoader() == null,
                "a primitive array has the null (bootstrap) loader");
        check(String[].class.getClassLoader() == null,
                "a String[] has the same (null) loader as String");
        check(String[].class.getComponentType() == String.class,
                "component type of String[] is String");
        check(int[][].class.getComponentType() == int[].class,
                "component type of int[][] is int[]");
        check(Class.forName("[Ljava.lang.String;") == String[].class,
                "an array class is identified by its descriptor name");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
