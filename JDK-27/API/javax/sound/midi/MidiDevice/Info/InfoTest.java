/*
 * @test
 * @summary javax.sound.midi.MidiDevice.Info conformance: white-box structure + black-box behavior.
 * @run main InfoTest
 */

import java.lang.reflect.Modifier;
import javax.sound.midi.MidiDevice;

public class InfoTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.sound.midi.MidiDevice$Info");
        check(!c.isInterface(), "is not interface");
        check(!Modifier.isAbstract(c.getModifiers()), "is not abstract");
        check(Modifier.isPublic(c.getModifiers()), "is public");
        check(c.getSuperclass() != null && c.getSuperclass().getName().equals("java.lang.Object"), "extends Object");
        check(c.getDeclaredConstructors().length == 1, "single declared constructor");
        check(Modifier.isProtected(c.getDeclaredConstructors()[0].getModifiers()), "protected constructor");
        String[] names = {"equals", "hashCode", "getName", "getVendor", "getDescription", "getVersion", "toString"};
        for (String n : names) {
            java.lang.reflect.Method m = c.getDeclaredMethod(n, n.equals("equals") ? new Class[]{Object.class} : new Class[0]);
            check(Modifier.isFinal(m.getModifiers()), n + " is final");
        }
    }

    static void testBlackBox() throws Exception {
        InfoImpl i1 = new InfoImpl("Name", "Vendor", "Description", "1.0");
        check("Name".equals(i1.getName()), "getName round-trip");
        check("Vendor".equals(i1.getVendor()), "getVendor round-trip");
        check("Description".equals(i1.getDescription()), "getDescription round-trip");
        check("1.0".equals(i1.getVersion()), "getVersion round-trip");
        check("Name".equals(i1.toString()), "toString returns name");
        InfoImpl sameFields = new InfoImpl("Name", "Vendor", "Description", "1.0");
        check(i1.equals(i1), "equals is reflexive");
        check(!i1.equals(sameFields), "equals is identity-based (distinct instance false)");
        check(!i1.equals(null), "not equal to null");
        check(!i1.equals("Name"), "not equal to a String");
        check(i1.hashCode() == i1.hashCode(), "hashCode stable");
        check(i1.hashCode() == System.identityHashCode(i1), "hashCode is identity hash");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}

class InfoImpl extends MidiDevice.Info {
    InfoImpl(String a, String b, String c, String d) { super(a, b, c, d); }
}
