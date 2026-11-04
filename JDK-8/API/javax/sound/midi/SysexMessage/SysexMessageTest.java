/*
 * @test
 * @summary javax.sound.midi.SysexMessage conformance: behavioral.
 * @run main SysexMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class SysexMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] d = new byte[]{0x01, 0x02};
        javax.sound.midi.SysexMessage m = new javax.sound.midi.SysexMessage(javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE, d, 2);
        check(m.getStatus() == javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE, "status");
        check(m.getData().length == 2, "data length");
        check(m.getData()[0] == 0x01, "data0");
        javax.sound.midi.SysexMessage m2 = new javax.sound.midi.SysexMessage(javax.sound.midi.SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, d, 2);
        check(m2.getStatus() == javax.sound.midi.SysexMessage.SPECIAL_SYSTEM_EXCLUSIVE, "special status");
        javax.sound.midi.SysexMessage c = (javax.sound.midi.SysexMessage) m.clone();
        check(c.getStatus() == javax.sound.midi.SysexMessage.SYSTEM_EXCLUSIVE, "clone");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
