/*
 * @test
 * @summary javax.swing.text.NavigationFilter conformance: default setDot/moveDot delegation to FilterBypass with exact arguments (headless-safe).
 * @run main NavigationFilterTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Set;
import javax.swing.text.BadLocationException;
import javax.swing.text.NavigationFilter;
import javax.swing.text.Position;

public class NavigationFilterTest {
    static int passed;

    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static boolean hasInterface(Class<?> c, String name) {
        for (Class<?> i : c.getInterfaces()) if (i.getName().equals(name)) return true;
        return false;
    }

    /** Records every call made through the bypass. */
    static class RecordingBypass extends NavigationFilter.FilterBypass {
        String log = "";
        public void setDot(int dot, Position.Bias bias) { log += "setDot:" + dot + ":" + bias + ";"; }
        public void moveDot(int dot, Position.Bias bias) { log += "moveDot:" + dot + ":" + bias + ";"; }
        public javax.swing.text.Caret getCaret() { return null; }
    }

    /** A filter that constrains navigation to the even offsets it lets through. */
    static class Clamping extends NavigationFilter {
        NavigationFilter base = new NavigationFilter();
        public void setDot(FilterBypass fb, int dot, Position.Bias bias) {
            log += "f.setDot:" + dot + ";";
            base.setDot(fb, dot, bias); // default behaviour: straight through
        }
        public void moveDot(FilterBypass fb, int dot, Position.Bias bias) {
            log += "f.moveDot:" + dot + ";";
            base.moveDot(fb, dot, bias);
        }
        String log = "";
    }

    static void testTypeShape() throws Exception {
        Class<?> c = Class.forName("javax.swing.text.NavigationFilter", false, NavigationFilterTest.class.getClassLoader());
        check(c != null, "forName");
        check(!c.isInterface(), "is class");
        check(!c.isEnum() && !c.isAnnotation(), "not enum/annotation");
        check(!Modifier.isAbstract(c.getModifiers()), "not abstract");
        check(!Modifier.isFinal(c.getModifiers()), "not final");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass");
        Set<String> names = new HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("setDot"), "method setDot");
        check(names.contains("moveDot"), "method moveDot");
        check(names.contains("getNextVisualPositionFrom"), "method getNextVisualPositionFrom");
        Class<?> fb = Class.forName("javax.swing.text.NavigationFilter$FilterBypass", false, NavigationFilterTest.class.getClassLoader());
        check(Modifier.isAbstract(fb.getModifiers()), "FilterBypass abstract");
        int abstractCount = 0;
        for (Method m : fb.getMethods()) if (Modifier.isAbstract(m.getModifiers())) abstractCount++;
        check(abstractCount >= 2, "FilterBypass has abstract setDot/moveDot");
    }

    static void testDefaultDelegation() throws Exception {
        // plain filter: every call goes straight to the bypass untouched
        NavigationFilter plain = new NavigationFilter();
        RecordingBypass rb = new RecordingBypass();
        plain.setDot(rb, 7, Position.Bias.Forward);
        check(rb.log.equals("setDot:7:Forward;"), "default setDot delegates verbatim, got [" + rb.log + "]");
        plain.moveDot(rb, 3, Position.Bias.Backward);
        check(rb.log.equals("setDot:7:Forward;moveDot:3:Backward;"), "default moveDot delegates verbatim");
        // null bias is forwarded as-is
        plain.setDot(rb, 0, null);
        check(rb.log.endsWith("setDot:0:null;"), "null bias forwarded");
    }

    static void testOverrideChainsToDefault() throws Exception {
        Clamping f = new Clamping();
        RecordingBypass rb = new RecordingBypass();
        f.setDot((NavigationFilter.FilterBypass) rb, 5, Position.Bias.Forward);
        check(f.log.equals("f.setDot:5;"), "override observed the call");
        check(rb.log.equals("setDot:5:Forward;"), "override forwarded via default");
        f.moveDot((NavigationFilter.FilterBypass) rb, 9, Position.Bias.Forward);
        check(f.log.equals("f.setDot:5;f.moveDot:9;"), "moveDot override observed");
        check(rb.log.equals("setDot:5:Forward;moveDot:9:Forward;"), "moveDot forwarded verbatim");
    }

    public static void main(String[] args) throws Exception {
        testTypeShape();
        testDefaultDelegation();
        testOverrideChainsToDefault();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
