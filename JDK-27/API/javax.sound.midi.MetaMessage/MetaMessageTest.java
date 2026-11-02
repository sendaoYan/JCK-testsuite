/*
 * @test
 * @summary javax.sound.midi.MetaMessage conformance: behavioral.
 * @run main MetaMessageTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class MetaMessageTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        byte[] d = new byte[]{0x01, 0x02, 0x03};
        javax.sound.midi.MetaMessage m = new javax.sound.midi.MetaMessage(0x2F, d, 3);
        check(m.getType() == 0x2F, "type");
        check(m.getStatus() == 0xFF, "status");
        check(m.getData().length == 3, "data length");
        check(m.getData()[0] == 0x01, "data0");
        javax.sound.midi.MetaMessage c = (javax.sound.midi.MetaMessage) m.clone();
        check(c.getType() == 0x2F, "clone type");
        javax.sound.midi.MetaMessage m2 = new javax.sound.midi.MetaMessage();
        m2.setMessage(0x51, d, 3);
        check(m2.getType() == 0x51, "setMessage type");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
