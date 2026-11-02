/*
 * @test
 * @summary java.net.MulticastSocket conformance: API type verification.
 * @run main MulticastSocketTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MulticastSocketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        java.net.MulticastSocket ms = new java.net.MulticastSocket();
check(ms.isBound(), "bound");
check(ms instanceof java.net.DatagramSocket, "DatagramSocket");
ms.close();
check(ms.isClosed(), "closed");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
