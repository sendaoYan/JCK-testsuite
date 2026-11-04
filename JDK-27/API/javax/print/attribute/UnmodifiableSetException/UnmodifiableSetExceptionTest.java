/*
 * @test
 * @summary javax.print.attribute.UnmodifiableSetException conformance: white-box structure + black-box behavior.
 * @run main UnmodifiableSetExceptionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.net.*;
import java.io.*;
import javax.print.*;
import javax.print.attribute.*;
import javax.print.attribute.standard.*;
import javax.print.event.*;

public class UnmodifiableSetExceptionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static Class<?> load(String n) throws Exception { return Class.forName(n); }
    static boolean hasMethod(Class<?> k, String n) {
        for (Method m : k.getMethods()) if (m.getName().equals(n)) return true;
        return false;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = load("javax.print.attribute.UnmodifiableSetException");
        check(Modifier.isPublic(c.getModifiers()) && !c.isInterface(), "public class");
        check(RuntimeException.class.isAssignableFrom(c), "extends RuntimeException");
    }


    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        check(new UnmodifiableSetException().getMessage() == null, "no-arg ctor");
        check("msg".equals(new UnmodifiableSetException("msg").getMessage()), "message ctor");
    }


    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
