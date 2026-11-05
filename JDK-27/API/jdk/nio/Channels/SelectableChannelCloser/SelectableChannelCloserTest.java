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


    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.nio.Channels$SelectableChannelCloser");
        java.util.Set<String> declared = new java.util.TreeSet<>();
        for (Method m : c.getDeclaredMethods()) {
            check(Modifier.isPublic(m.getModifiers()) && Modifier.isAbstract(m.getModifiers()), m.getName() + " public abstract");
            check(m.getParameterCount() == 1 && m.getParameterTypes()[0].getName().equals("java.nio.channels.SelectableChannel"),
                    m.getName() + " takes SelectableChannel");
            check(m.getExceptionTypes().length == 1 && m.getExceptionTypes()[0] == java.io.IOException.class,
                    m.getName() + " declares IOException");
            declared.add(m.getName());
        }
        check(declared.equals(new java.util.TreeSet<>(java.util.Arrays.asList("implCloseChannel", "implReleaseChannel"))),
                "exactly two methods");
        check(c.getFields().length == 0, "no constants");
        check(c.isMemberClass() && Modifier.isPublic(c.getModifiers()), "public member of jdk.nio.Channels");
        // wire-up contract exercised through jdk.nio.Channels.readWriteSelectableChannel
        java.io.File f = java.io.File.createTempFile("closerdeepen", ".tmp");
        f.deleteOnExit();
        java.io.FileOutputStream fos = new java.io.FileOutputStream(f);
        final int[] calls = new int[2];
        java.nio.channels.SelectableChannel ch = jdk.nio.Channels.readWriteSelectableChannel(fos.getFD(), new jdk.nio.Channels.SelectableChannelCloser() {
            public void implCloseChannel(java.nio.channels.SelectableChannel c) { calls[0]++; }
            public void implReleaseChannel(java.nio.channels.SelectableChannel c) { calls[1]++; }
        });
        ch.close();
        ch.close();
        check(calls[0] == 1, "implCloseChannel called once even on double close");
        fos.close();
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.nio.Channels$SelectableChannelCloser");
        check(c.isInterface(), "isInterface");
        check(c.getInterfaces().length == 0, "no superinterfaces");
        check(hasMethod(c, "implCloseChannel"), "declares implCloseChannel");
        check(hasMethod(c, "implReleaseChannel"), "declares implReleaseChannel");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
