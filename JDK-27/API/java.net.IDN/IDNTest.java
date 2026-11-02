/*
 * @test
 * @summary java.net.IDN conformance: API type verification.
 * @run main IDNTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class IDNTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(java.net.IDN.toASCII("bücher.de").equals("xn--bcher-kva.de"), "toASCII");
check(java.net.IDN.toUnicode("xn--bcher-kva.de").equals("bücher.de"), "toUnicode");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
