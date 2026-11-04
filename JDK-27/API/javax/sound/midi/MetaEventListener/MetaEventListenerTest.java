/*
 * @test
 * @summary javax.sound.midi.MetaEventListener conformance: white-box structure + black-box behavior.
 * @run main MetaEventListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.MetaEventListener;
import javax.sound.midi.MetaMessage;

public class MetaEventListenerTest {
    static int passed;
    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }
    static Set<String> interfaceNames(Class<?> c) {
        Set<String> s = new HashSet<>();
        for (Class<?> i : c.getInterfaces()) s.add(i.getName());
        return s;
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MetaEventListener");
        check(c != null, "loads");
        int mods = c.getModifiers();
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(mods), "public modifier");
        check(c.isAnnotation() == false, "is annotation");
        check(c.getSuperclass() == null, "no superclass");
        check(c.getInterfaces().length == 1, "direct interface count");
        check(interfaceNames(c).contains("java.util.EventListener"), "extends java.util.EventListener");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "declared method count");
        check(ms[0].getName().equals("meta"), "method meta");
        check(ms[0].getParameterTypes().length == 1
                && ms[0].getParameterTypes()[0].getName().equals("javax.sound.midi.MetaMessage"),
                "meta(MetaMessage) parameter");
        check(ms[0].getReturnType() == void.class, "meta returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The interface's sole contract is the listener callback signature.
        MetaMessage msg = new MetaMessage();
        msg.setMessage(0x51, new byte[]{0x07, (byte) 0xA1, 0x20}, 3); // tempo meta message
        final MetaMessage[] received = new MetaMessage[1];
        final int[] count = new int[1];
        MetaEventListener listener = m -> { received[0] = m; count[0]++; };
        check(listener != null, "listener instantiable");
        listener.meta(msg);
        check(received[0] == msg, "callback receives same MetaMessage instance");
        check(count[0] == 1, "callback invoked exactly once");
        listener.meta(msg);
        check(count[0] == 2, "callback invoked again");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
