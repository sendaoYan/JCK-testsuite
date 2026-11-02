/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult.HandshakeStatus conformance: API type verification.
 * @run main HandshakeStatusTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class HandshakeStatusTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(javax.net.ssl.SSLEngineResult.HandshakeStatus.valueOf("FINISHED") == javax.net.ssl.SSLEngineResult.HandshakeStatus.FINISHED, "valueOf");
check(javax.net.ssl.SSLEngineResult.HandshakeStatus.values().length == 6, "6 values");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
