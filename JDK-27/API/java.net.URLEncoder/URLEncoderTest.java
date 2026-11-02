/*
 * @test
 * @summary java.net.URLEncoder conformance: API type verification.
 * @run main URLEncoderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLEncoderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.URLEncoder.encode("a b", "UTF-8").equals("a+b"), "encode");
check(java.net.URLEncoder.encode("a b").equals("a+b"), "encode default charset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
