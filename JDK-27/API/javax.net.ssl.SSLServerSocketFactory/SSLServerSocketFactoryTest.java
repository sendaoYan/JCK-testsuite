/*
 * @test
 * @summary javax.net.ssl.SSLServerSocketFactory conformance: API type verification.
 * @run main SSLServerSocketFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SSLServerSocketFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        javax.net.ssl.SSLServerSocketFactory f = (javax.net.ssl.SSLServerSocketFactory) javax.net.ssl.SSLServerSocketFactory.getDefault();
check(f != null, "getDefault");
check(f.getDefaultCipherSuites().length > 0, "default ciphers");
check(f.getSupportedCipherSuites().length > 0, "supported ciphers");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
