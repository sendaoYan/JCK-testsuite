/*
 * @test
 * @summary javax.sql.rowset.RowSetFactory conformance: interface shape, supertypes and constants
 * @run main RowSetFactoryTest
 */

import java.lang.reflect.Modifier;

public class RowSetFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("javax.sql.rowset.RowSetFactory");
        check(c != null, "loads");
        check(c.isInterface(), "is interface");
        check(!c.isEnum(), "not enum");
        check(!c.isAnnotation(), "not annotation");
        check(java.lang.reflect.Modifier.isPublic(c.getModifiers()), "public");
        Class<?>[] ifs = c.getInterfaces();
        check(ifs.length == 0, "0 direct superinterface(s)");
        check(c.getDeclaredMethods().length == 5, "declared method count 5");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
