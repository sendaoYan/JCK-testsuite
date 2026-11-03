/*
 * @test
 * @summary javax.sound.midi.SoundbankResource conformance: white-box structure + black-box behavior.
 * @run main SoundbankResourceTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.Soundbank;
import javax.sound.midi.SoundbankResource;

public class SoundbankResourceTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.SoundbankResource");
        check(!c.isInterface(), "is not interface");
        check(Modifier.isAbstract(c.getModifiers()), "is abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        java.util.Set<String> methods = new java.util.HashSet<>();
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) methods.add(m.getName());
        check(methods.contains("getSoundbank"), "method getSoundbank");
        check(methods.contains("getName"), "method getName");
        check(methods.contains("getDataClass"), "method getDataClass");
        check(methods.contains("getData"), "method getData");
    }

    static void testBlackBox() throws Exception {
        FakeSB sb = new FakeSB();
        Object payload = new byte[]{10, 20, 30};
        Res r = new Res(sb, "Layer 1", byte[].class, payload);
        check(r.getSoundbank() == sb, "getSoundbank round-trip");
        check("Layer 1".equals(r.getName()), "getName round-trip");
        check(r.getDataClass() == byte[].class, "getDataClass round-trip");
        check(r.getData() == payload, "getData returns payload");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class Res extends SoundbankResource {
    private final Object data;
    Res(Soundbank sb, String name, Class<?> dc, Object d) { super(sb, name, dc); data = d; }
    public Object getData() { return data; }
}

class FakeSB implements Soundbank {
    public String getName() { return "SB"; }
    public String getVersion() { return "1"; }
    public String getVendor() { return "V"; }
    public String getDescription() { return "D"; }
    public SoundbankResource[] getResources() { return new SoundbankResource[0]; }
    public javax.sound.midi.Instrument[] getInstruments() { return new javax.sound.midi.Instrument[0]; }
    public javax.sound.midi.Instrument getInstrument(javax.sound.midi.Patch p) { return null; }
}
