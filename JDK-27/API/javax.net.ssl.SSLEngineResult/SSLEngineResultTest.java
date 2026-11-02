/*
 * @test
 * @summary javax.net.ssl.SSLEngineResult conformance: API type verification.
 * @run main SSLEngineResultTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLEngineResultTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLEngineResult r = new javax.net.ssl.SSLEngineResult(
    javax.net.ssl.SSLEngineResult.Status.OK,
    javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, 3, 4);
check(r.getStatus() == javax.net.ssl.SSLEngineResult.Status.OK, "status");
check(r.getHandshakeStatus() == javax.net.ssl.SSLEngineResult.HandshakeStatus.NOT_HANDSHAKING, "handshake");
check(r.bytesConsumed() == 3, "consumed");
check(r.bytesProduced() == 4, "produced");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
