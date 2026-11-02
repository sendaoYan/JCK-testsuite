/*
 * @test
 * @summary javax.net.ssl.TrustManagerFactory conformance: API type verification.
 * @run main TrustManagerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class TrustManagerFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        String alg = javax.net.ssl.TrustManagerFactory.getDefaultAlgorithm();
check(alg != null && alg.length() > 0, "default algorithm");
javax.net.ssl.TrustManagerFactory tf = javax.net.ssl.TrustManagerFactory.getInstance(alg);
check(tf.getAlgorithm().equals(alg), "algorithm");
check(tf.getProvider() != null, "provider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
