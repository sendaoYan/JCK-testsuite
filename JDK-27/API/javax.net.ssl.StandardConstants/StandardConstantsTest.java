/*
 * @test
 * @summary javax.net.ssl.StandardConstants conformance: API type verification.
 * @run main StandardConstantsTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class StandardConstantsTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        check(javax.net.ssl.StandardConstants.SNI_HOST_NAME == 0, "SNI_HOST_NAME");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
