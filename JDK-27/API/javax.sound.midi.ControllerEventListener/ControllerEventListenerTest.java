/*
 * @test
 * @summary javax.sound.midi.ControllerEventListener conformance: white-box structure + black-box behavior.
 * @run main ControllerEventListenerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.sound.midi.ControllerEventListener;
import javax.sound.midi.ShortMessage;

public class ControllerEventListenerTest {
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
        Class<?> c = Class.forName("javax.sound.midi.ControllerEventListener");
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
        check(ms[0].getName().equals("controlChange"), "method controlChange");
        check(ms[0].getParameterTypes().length == 1
                && ms[0].getParameterTypes()[0].getName().equals("javax.sound.midi.ShortMessage"),
                "controlChange(ShortMessage) parameter");
        check(ms[0].getReturnType() == void.class, "controlChange returns void");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // The interface's sole contract is the listener callback signature.
        ShortMessage msg = new ShortMessage();
        msg.setMessage(ShortMessage.CONTROL_CHANGE, 0, 7, 100);
        final ShortMessage[] received = new ShortMessage[1];
        final int[] count = new int[1];
        ControllerEventListener listener = m -> { received[0] = m; count[0]++; };
        check(listener != null, "listener instantiable");
        listener.controlChange(msg);
        check(received[0] == msg, "callback receives same ShortMessage instance");
        check(count[0] == 1, "callback invoked exactly once");
        // Multiple invocations accumulate.
        listener.controlChange(msg);
        check(count[0] == 2, "callback invoked again");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
