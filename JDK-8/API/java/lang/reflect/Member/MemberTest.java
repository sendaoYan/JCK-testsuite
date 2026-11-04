/*
 * @test
 * @summary java.lang.reflect.Member conformance: declaring class/name/modifiers and constants.
 * @run main MemberTest
 */

import java.lang.reflect.Member;
import java.lang.reflect.Modifier;

public class MemberTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static class Sample {
        public int f;
    }

    static void testMember() throws Exception {
        Member m = Sample.class.getField("f");
        check(m.getDeclaringClass() == Sample.class, "getDeclaringClass");
        check("f".equals(m.getName()), "getName");
        check(m.getModifiers() == Modifier.PUBLIC, "getModifiers");
        check(!m.isSynthetic(), "isSynthetic false");
        check(Member.PUBLIC == 0, "PUBLIC == 0");
        check(Member.DECLARED == 1, "DECLARED == 1");
    }

    public static void main(String[] args) throws Exception {
        testMember();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
