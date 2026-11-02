/*
 * @test
 * @summary javax.net.ssl.SSLSocketFactory conformance: API type verification.
 * @run main SSLSocketFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLSocketFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLSocketFactory f = (javax.net.ssl.SSLSocketFactory) javax.net.ssl.SSLSocketFactory.getDefault();
check(f != null, "getDefault");
check(f.getDefaultCipherSuites().length > 0, "default ciphers");
check(f.getSupportedCipherSuites().length > 0, "supported ciphers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
