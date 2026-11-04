/*
 * @test
 * @summary javax.sound.sampled.LineEvent conformance: behavioral.
 * @run main LineEventTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;

public class LineEventTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        final javax.sound.sampled.Line line = (javax.sound.sampled.Line) java.lang.reflect.Proxy.newProxyInstance(
            javax.sound.sampled.Line.class.getClassLoader(),
            new Class<?>[]{javax.sound.sampled.Line.class},
            (proxy, method, args) -> {
                Class<?> rt = method.getReturnType();
                if (rt == boolean.class) return false;
                if (rt == int.class) return 0;
                return null;
            });
        javax.sound.sampled.LineEvent ev = new javax.sound.sampled.LineEvent(line, javax.sound.sampled.LineEvent.Type.OPEN, 42L);
        check(ev.getLine() == line, "getLine");
        check(ev.getType() == javax.sound.sampled.LineEvent.Type.OPEN, "getType");
        check(ev.getFramePosition() == 42L, "getFramePosition");
        check(ev.toString() != null, "toString");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
