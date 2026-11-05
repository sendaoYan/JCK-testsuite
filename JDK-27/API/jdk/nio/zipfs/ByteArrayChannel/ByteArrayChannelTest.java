/*
 * @test
 * @summary jdk.nio.zipfs.ByteArrayChannel conformance: internal class reflection.
 * @modules jdk.zipfs/jdk.nio.zipfs:+open
 * @run main ByteArrayChannelTest
 */
import java.lang.reflect.Constructor;

import java.lang.reflect.Method;

public class ByteArrayChannelTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testDeepen() throws Exception {
        Class<?> c = Class.forName("jdk.nio.zipfs.ByteArrayChannel");
        Constructor<?> grow = c.getDeclaredConstructor(int.class, boolean.class);
        Constructor<?> wrap = c.getDeclaredConstructor(byte[].class, boolean.class);
        grow.setAccessible(true);
        wrap.setAccessible(true);
        java.nio.channels.SeekableByteChannel ch = (java.nio.channels.SeekableByteChannel) grow.newInstance(4, false);
        check(ch.size() == 0 && ch.position() == 0 && ch.isOpen(), "fresh empty channel");
        java.nio.ByteBuffer wb = java.nio.ByteBuffer.wrap(new byte[] {1, 2, 3});
        check(ch.write(wb) == 3, "write returns count");
        check(ch.size() == 3 && ch.position() == 3, "size/position advance together");
        java.nio.ByteBuffer rb = java.nio.ByteBuffer.allocate(3);
        ch.position(0);
        check(ch.read(rb) == 3, "read full");
        check(rb.array()[0] == 1 && rb.array()[2] == 3, "read content");
        check(ch.position() == 3, "position after read");
        ch.position(2);
        check(ch.position() == 2, "position setter");
        boolean uoe = false;
        try { ch.truncate(1); } catch (UnsupportedOperationException e) { uoe = true; }
        check(uoe, "truncate unimplemented UOE");
        ch.position(0);
        Method toByteArray = c.getDeclaredMethod("toByteArray");
        toByteArray.setAccessible(true);
        byte[] snapshot = (byte[]) toByteArray.invoke(ch);
        check(snapshot.length == 3 && snapshot[0] == 1 && snapshot[2] == 3, "toByteArray snapshot from position 0");
        ch.close();
        check(!ch.isOpen(), "closed");
        boolean cce = false;
        try { ch.read(java.nio.ByteBuffer.allocate(1)); } catch (java.nio.channels.ClosedChannelException e) { cce = true; }
        check(cce, "read after close ClosedChannelException");
        java.nio.channels.SeekableByteChannel ro = (java.nio.channels.SeekableByteChannel) wrap.newInstance(new byte[] {9, 8, 7}, true);
        boolean nwce = false;
        try { ro.write(java.nio.ByteBuffer.wrap(new byte[] {1})); } catch (java.nio.channels.NonWritableChannelException e) { nwce = true; }
        check(nwce, "readonly write NonWritableChannelException");
        java.nio.ByteBuffer rb2 = java.nio.ByteBuffer.allocate(3);
        check(ro.read(rb2) == 3 && rb2.array()[0] == 9 && rb2.array()[2] == 7, "readonly read wraps buffer");
        check(ro.size() == 3, "wrapped buffer size");
    }

    public static void main(String[] args) throws Exception {
        testDeepen();

        Class<?> c = Class.forName("jdk.nio.zipfs.ByteArrayChannel", false, ByteArrayChannelTest.class.getClassLoader());
        check(c != null, "loads");
        check(c.getName().equals("jdk.nio.zipfs.ByteArrayChannel"), "name");
        check(c.isInterface() == false, "isInterface");
        check(c.isEnum() == false, "isEnum");
        check(c.isAnnotation() == false, "isAnnotation");
        check(java.lang.reflect.Modifier.isAbstract(c.getModifiers()) == false, "abstract");
        check(java.lang.reflect.Modifier.isFinal(c.getModifiers()) == false, "final");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        {
            java.lang.Class<?>[] arr = c.getInterfaces();
            String[] n = new String[arr.length];
            for (int i = 0; i < arr.length; i++) n[i] = arr[i].getName();
            java.util.Arrays.sort(n);
            check(String.join(";", n).equals("java.nio.channels.SeekableByteChannel"), "interfaces");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Method m : c.getDeclaredMethods()) {
                int mod = m.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(m.getName());
            }
            check(String.join(";", s).equals("close;isOpen;position;read;size;toByteArray;truncate;write"), "methods");
        }
        {
            java.util.TreeSet<String> s = new java.util.TreeSet<>();
            for (java.lang.reflect.Field f : c.getDeclaredFields()) {
                int mod = f.getModifiers();
                if (java.lang.reflect.Modifier.isPublic(mod) || java.lang.reflect.Modifier.isProtected(mod)) s.add(f.getName());
            }
            check(String.join(";", s).equals(""), "fields");
        }
        System.out.println("PASS (" + passed + " assertions)");
    }
}
