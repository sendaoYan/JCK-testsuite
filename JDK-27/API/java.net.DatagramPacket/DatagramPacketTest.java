/*
 * @test
 * @summary java.net.DatagramPacket conformance: API type verification.
 * @run main DatagramPacketTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class DatagramPacketTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) throws Exception {
        byte[] buf = new byte[64];
java.net.DatagramPacket p = new java.net.DatagramPacket(buf, 10);
check(p.getLength() == 10, "length");
check(p.getOffset() == 0, "offset");
check(p.getPort() == 0, "default port 0");
p.setLength(20);
check(p.getLength() == 20, "setLength");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
