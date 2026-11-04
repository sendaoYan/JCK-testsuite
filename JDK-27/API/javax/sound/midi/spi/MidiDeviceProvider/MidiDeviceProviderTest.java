/*
 * @test
 * @summary javax.sound.midi.spi.MidiDeviceProvider conformance: white-box structure + black-box behavior.
 * @run main MidiDeviceProviderTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.MidiDevice;
import javax.sound.midi.spi.MidiDeviceProvider;

public class MidiDeviceProviderTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.spi.MidiDeviceProvider");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("isDeviceSupported"), "method isDeviceSupported");
        check(methods.contains("getDeviceInfo"), "method getDeviceInfo");
        check(methods.contains("getDevice"), "method getDevice");
    }

    static void testBlackBox() throws Exception {
        MidiDevice.Info a = new InfoX("A");
        MidiDevice.Info b = new InfoX("B");
        FakeProvider p = new FakeProvider(new MidiDevice.Info[]{a, b});
        check(p.getDeviceInfo() != null, "getDeviceInfo non-null");
        check(p.getDeviceInfo().length == 2, "getDeviceInfo length");
        check(p.getDeviceInfo()[0] == a, "getDeviceInfo[0] instance");
        check(p.isDeviceSupported(a), "isDeviceSupported true for known info");
        check(p.isDeviceSupported(b), "isDeviceSupported true for second");
        check(!p.isDeviceSupported(new InfoX("C")), "isDeviceSupported false for unknown info");
        check(p.getDevice(a) != null, "getDevice returns device for known info");
        check(p.getDevice(a) == p.getDevice(a), "getDevice deterministic instance");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class InfoX extends MidiDevice.Info {
    InfoX(String n) { super(n, n, n, n); }
}

class FakeProvider extends MidiDeviceProvider {
    private final MidiDevice.Info[] infos;
    private final MidiDevice dev = new FakeDevice();
    FakeProvider(MidiDevice.Info[] i) { infos = i; }
    public MidiDevice.Info[] getDeviceInfo() { return infos; }
    public MidiDevice getDevice(MidiDevice.Info info) { return dev; }
}

class FakeDevice implements MidiDevice {
    public MidiDevice.Info getDeviceInfo() { return null; }
    public void open() {}
    public void close() {}
    public boolean isOpen() { return false; }
    public long getMicrosecondPosition() { return 0; }
    public int getMaxReceivers() { return 0; }
    public int getMaxTransmitters() { return 0; }
    public javax.sound.midi.Receiver getReceiver() { return null; }
    public java.util.List<javax.sound.midi.Receiver> getReceivers() { return java.util.Collections.emptyList(); }
    public javax.sound.midi.Transmitter getTransmitter() { return null; }
    public java.util.List<javax.sound.midi.Transmitter> getTransmitters() { return java.util.Collections.emptyList(); }
}
