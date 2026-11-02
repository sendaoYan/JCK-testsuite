/*
 * @test
 * @summary jdk.nio.Channels.SelectableChannelCloser conformance: interface.
 * @run main SelectableChannelCloserTest
 */

import java.lang.reflect.*;


public class SelectableChannelCloserTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) {
        for (Method m : c.getDeclaredMethods()) if (m.getName().equals(n)) return true;
        return false;
    }
    static boolean hasInterface(Class<?> c, String n) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(n)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("jdk.nio.Channels$SelectableChannelCloser");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "implCloseChannel"), "declares implCloseChannel");
        check(hasMethod(c, "implReleaseChannel"), "declares implReleaseChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
