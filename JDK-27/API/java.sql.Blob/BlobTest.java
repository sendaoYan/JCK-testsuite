/*
 * @test
 * @summary java.sql.Blob conformance: interface shape, supertypes and constants
 * @run main BlobTest
 */

import java.lang.reflect.Modifier;

public class BlobTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.sql.Blob");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 1, "1 direct superinterface(s)");
        check(ifs[0].getName().equals("java.lang.AutoCloseable"), "superinterface 0");
        check(c.getDeclaredMethods().length == 12, "declared method count 12");        System.out.println("PASS (" + passed + " assertions)");
    }
}
