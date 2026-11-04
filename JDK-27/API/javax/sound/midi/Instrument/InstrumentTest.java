/*
 * @test
 * @summary javax.sound.midi.Instrument conformance: white-box structure + black-box behavior.
 * @run main InstrumentTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

public class InstrumentTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Instrument");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("javax.sound.midi.SoundbankResource"), "extends SoundbankResource");
        check(c.getInterfaces().length == 0, "no direct interfaces");
        boolean hasGetPatch = false;
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals("getPatch")) hasGetPatch = true;
        check(hasGetPatch, "declares getPatch");
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB("Bank", "1.0", "Vendor", "desc");
        Patch p = new Patch(3, 42);
        Inst inst = new Inst(sb, p, "Grand Piano", byte[].class);
        check(inst.getPatch() == p, "getPatch returns the constructed Patch instance");
        check(inst.getPatch().getBank() == 3, "Patch bank round-trip");
        check(inst.getPatch().getProgram() == 42, "Patch program round-trip");
        check("Grand Piano".equals(inst.getName()), "inherited getName round-trip");
        check(inst.getDataClass() == byte[].class, "inherited getDataClass round-trip");
        check(inst.getSoundbank() == sb, "inherited getSoundbank round-trip");
        check(inst.getData() != null, "getData returns resource payload");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class Inst extends Instrument {
    private final Object data;
    Inst(Soundbank sb, Patch p, String name, Class<?> dc) { super(sb, p, name, dc); this.data = new Object(); }
    public Object getData() { return data; }
}

class FakeSB implements Soundbank {
    private final String name, version, vendor, description;
    FakeSB(String n, String v, String vd, String d) { name = n; version = v; vendor = vd; description = d; }
    public String getName() { return name; }
    public String getVersion() { return version; }
    public String getVendor() { return vendor; }
    public String getDescription() { return description; }
    public SoundbankResource[] getResources() { return new SoundbankResource[0]; }
    public Instrument[] getInstruments() { return new Instrument[0]; }
    public Instrument getInstrument(Patch p) { return null; }
}
