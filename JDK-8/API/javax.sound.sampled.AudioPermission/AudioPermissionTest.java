/*
 * @test
 * @summary javax.sound.sampled.AudioPermission conformance: behavioral.
 * @run main AudioPermissionTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class AudioPermissionTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        javax.sound.sampled.AudioPermission p = new javax.sound.sampled.AudioPermission("play");
        check("play".equals(p.getName()), "name");
        check(p instanceof java.security.Permission, "is Permission");
        javax.sound.sampled.AudioPermission p2 = new javax.sound.sampled.AudioPermission("record", "read");
        check("record".equals(p2.getName()), "name2");
        check("".equals(p2.getActions()), "actions empty");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
