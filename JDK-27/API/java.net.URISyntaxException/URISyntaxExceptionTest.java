/*
 * @test
 * @summary java.net.URISyntaxException conformance: white-box structure + black-box behavior.
 * @run main URISyntaxExceptionTest
 */

import java.lang.reflect.*;
import java.net.*;

public class URISyntaxExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.net.URISyntaxException");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getSuperclass() == Exception.class, "extends Exception");
        check(c.getConstructor(String.class, String.class) != null, "ctor(input,reason)");
        check(c.getConstructor(String.class, String.class, int.class) != null, "ctor(input,reason,index)");
        check(c.getMethod("getInput") != null, "getInput");
        check(c.getMethod("getReason") != null, "getReason");
        check(c.getMethod("getIndex") != null, "getIndex");
        check(c.getMethod("getMessage") != null, "getMessage");
    }

    static void testBlackBox() throws Exception {
        URISyntaxException e = new URISyntaxException("input string", "reason");
        check(e.getInput().equals("input string"), "input");
        check(e.getReason().equals("reason"), "reason");
        check(e.getIndex() == -1, "index default -1");
        check(e.getMessage().contains("reason"), "message contains reason");
        check(e.getMessage().contains("input string"), "message contains input");

        URISyntaxException e2 = new URISyntaxException("in", "r", 7);
        check(e2.getInput().equals("in"), "input with index");
        check(e2.getReason().equals("r"), "reason with index");
        check(e2.getIndex() == 7, "index");

        check(e instanceof Exception, "instanceof Exception");
        try { throw e; } catch (Exception ex) { passed++; }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
