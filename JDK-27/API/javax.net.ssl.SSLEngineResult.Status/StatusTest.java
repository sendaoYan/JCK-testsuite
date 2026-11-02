/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult.Status conformance: API type verification.
 * @run main StatusTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StatusTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(javax.net.ssl.SSLEngineResult.Status.valueOf("CLOSED") == javax.net.ssl.SSLEngineResult.Status.CLOSED, "valueOf");
check(javax.net.ssl.SSLEngineResult.Status.values().length == 4, "4 values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
