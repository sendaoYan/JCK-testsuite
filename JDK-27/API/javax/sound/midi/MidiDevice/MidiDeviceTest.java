/*
 * @test
 * @summary javax.sound.midi.MidiDevice conformance: white-box structure + black-box behavior.
 * @key sound
 * @run main MidiDeviceTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.MidiSystem;
import javax.sound.midi.MidiUnavailableException;
import javax.sound.midi.Receiver;
import javax.sound.midi.Transmitter;

public class MidiDeviceTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDevice");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getInterfaces().length == 1, "one direct superinterface");
        check(c.getInterfaces()[0].getName().equals("java.lang.AutoCloseable"), "extends AutoCloseable");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        for (String n : new String[]{"getDeviceInfo", "open", "close", "isOpen", "getMicrosecondPosition", "getMaxReceivers", "getMaxTransmitters", "getReceiver", "getReceivers", "getTransmitter", "getTransmitters"})
            check(methods.contains(n), "method " + n);
    }

    static void testBlackBox() throws Exception {
        MidiDevice.Info[] infos = MidiSystem.getMidiDeviceInfo();
        check(infos != null, "getMidiDeviceInfo non-null array");
        for (MidiDevice.Info info : infos) {
            try {
                MidiDevice dev = MidiSystem.getMidiDevice(info);
                check(dev != null, "getMidiDevice non-null");
                check(dev.getDeviceInfo() != null, "getDeviceInfo non-null");
                check(!dev.isOpen(), "isOpen false before open");
                dev.open();
                check(dev.isOpen(), "isOpen true after open");
                check(dev.getMicrosecondPosition() >= 0L, "microsecond position non-negative");
                check(dev.getMaxReceivers() >= -1, "maxReceivers >= -1");
                check(dev.getMaxTransmitters() >= -1, "maxTransmitters >= -1");
                check(dev.getReceivers() != null, "getReceivers non-null");
                check(dev.getTransmitters() != null, "getTransmitters non-null");
                try {
                    Receiver r = dev.getReceiver();
                    check(r != null, "getReceiver non-null");
                    r.close();
                } catch (MidiUnavailableException expected) {
                    // device may legitimately expose no receivers
                }
                try {
                    Transmitter tx = dev.getTransmitter();
                    check(tx != null, "getTransmitter non-null");
                    tx.close();
                } catch (MidiUnavailableException expected) {
                    // device may legitimately expose no transmitters
                }
                dev.close();
                check(!dev.isOpen(), "isOpen false after close");
            } catch (MidiUnavailableException e) {
                // device could not be opened on this system; skip it
            }
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
