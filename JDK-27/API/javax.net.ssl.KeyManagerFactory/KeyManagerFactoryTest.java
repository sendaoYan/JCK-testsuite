/*
 * @test
 * @summary javax.net.ssl.KeyManagerFactory conformance: API type verification.
 * @run main KeyManagerFactoryTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class KeyManagerFactoryTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        String alg = javax.net.ssl.KeyManagerFactory.getDefaultAlgorithm();
check(alg != null && alg.length() > 0, "default algorithm");
javax.net.ssl.KeyManagerFactory kf = javax.net.ssl.KeyManagerFactory.getInstance(alg);
check(kf.getAlgorithm().equals(alg), "algorithm");
check(kf.getProvider() != null, "provider");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
