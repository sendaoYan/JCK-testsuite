/*
 * @test
 * @summary jdk.dynalink.beans.MissingMemberHandlerFactory conformance: interface reflection.
 * @run main MissingMemberHandlerFactoryTest
 */

import java.lang.reflect.*;

public class MissingMemberHandlerFactoryTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("jdk.dynalink.beans.MissingMemberHandlerFactory");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
