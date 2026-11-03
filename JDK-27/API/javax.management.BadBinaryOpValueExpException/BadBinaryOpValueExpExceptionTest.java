/*
 * @test
 * @summary javax.management.BadBinaryOpValueExpException conformance: white-box structure + black-box behavior.
 * @run main BadBinaryOpValueExpExceptionTest
 */

import java.lang.reflect.*;
import javax.management.*;

public class BadBinaryOpValueExpExceptionTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }


    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.management.BadBinaryOpValueExpException");
        check(!c.isInterface(), "concrete class, not interface");
        check((c.getModifiers() & Modifier.PUBLIC) != 0, "public class");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check("java.lang.Exception".equals(c.getSuperclass().getName()), "extends Exception");
        check(Throwable.class.isAssignableFrom(c), "is a Throwable");
        check(java.io.Serializable.class.isAssignableFrom(c), "serializable");
        check(c.getConstructor(javax.management.ValueExp.class) != null, "ValueExp constructor");
        c.getMethod("getExp");
        c.getMethod("toString");
    }

    static void testBlackBox() throws Exception {
        javax.management.StringValueExp exp = new javax.management.StringValueExp("op-expr");
        BadBinaryOpValueExpException e = new BadBinaryOpValueExpException(exp);
        check(e.getExp() == exp, "getExp round-trip");
        check("BadBinaryOpValueExpException: 'op-expr'".equals(e.toString()), "toString embeds exp");
        check(e.getMessage() == null, "no detail message");
        check(e instanceof Exception, "instanceof Exception");

        javax.management.StringValueExp exp2 = new javax.management.StringValueExp("other");
        BadBinaryOpValueExpException e2 = new BadBinaryOpValueExpException(exp2);
        check(e2.getExp() == exp2, "independent getExp round-trip");
        check("BadBinaryOpValueExpException: 'other'".equals(e2.toString()), "independent toString");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
