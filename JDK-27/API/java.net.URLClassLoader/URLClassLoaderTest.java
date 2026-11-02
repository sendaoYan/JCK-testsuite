/*
 * @test
 * @summary java.net.URLClassLoader conformance: API type verification.
 * @run main URLClassLoaderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLClassLoaderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.URLClassLoader cl = new java.net.URLClassLoader(new java.net.URL[0]);
check(cl.getURLs().length == 0, "no urls");
cl.close();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
