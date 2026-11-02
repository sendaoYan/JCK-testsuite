/*
 * @test
 * @summary java.net.URLDecoder conformance: API type verification.
 * @run main URLDecoderTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class URLDecoderTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.URLDecoder.decode("a+b", "UTF-8").equals("a b"), "decode");
check(java.net.URLDecoder.decode("a+b").equals("a b"), "decode default charset");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
