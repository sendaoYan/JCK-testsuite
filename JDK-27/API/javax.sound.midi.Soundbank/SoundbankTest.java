/*
 * @test
 * @summary javax.sound.midi.Soundbank conformance: white-box structure + black-box behavior.
 * @run main SoundbankTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Instrument;
import javax.sound.midi.Patch;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

public class SoundbankTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.Soundbank");
        check(c.isInterface(), "is interface");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        for (String n : new String[]{"getName", "getVersion", "getVendor", "getDescription", "getResources", "getInstruments", "getInstrument"})
            check(methods.contains(n), "method " + n);
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB();
        check("Emergency Bank".equals(sb.getName()), "getName round-trip");
        check("2.1".equals(sb.getVersion()), "getVersion round-trip");
        check("Vendor".equals(sb.getVendor()), "getVendor round-trip");
        check("Description".equals(sb.getDescription()), "getDescription round-trip");
        check(sb.getResources().length == 1, "getResources length");
        check(sb.getInstruments().length == 1, "getInstruments length");
        Instrument inst = sb.getInstruments()[0];
        check(sb.getInstrument(inst.getPatch()) == inst, "getInstrument(patch) returns instrument");
        check(sb.getInstrument(new Patch(99, 99)) == null, "getInstrument unknown patch null");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class FakeSB implements Soundbank {
    private final Instrument inst;
    private final SoundbankResource res;
    FakeSB() {
        inst = new FakeInstrument(this, new Patch(0, 1), "Piano", null);
        res = new FakeResource(this, "Wave", byte[].class);
    }
    public String getName() { return "Emergency Bank"; }
    public String getVersion() { return "2.1"; }
    public String getVendor() { return "Vendor"; }
    public String getDescription() { return "Description"; }
    public SoundbankResource[] getResources() { return new SoundbankResource[]{res}; }
    public Instrument[] getInstruments() { return new Instrument[]{inst}; }
    public Instrument getInstrument(Patch p) {
        Patch ip = inst.getPatch();
        return (ip.getBank() == p.getBank() && ip.getProgram() == p.getProgram()) ? inst : null;
    }
}

class FakeInstrument extends Instrument {
    FakeInstrument(Soundbank sb, Patch p, String name, Class<?> dc) { super(sb, p, name, dc); }
    public Object getData() { return null; }
}

class FakeResource extends SoundbankResource {
    FakeResource(Soundbank sb, String name, Class<?> dc) { super(sb, name, dc); }
    public Object getData() { return null; }
}
