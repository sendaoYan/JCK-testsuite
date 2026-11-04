/*
 * @test
 * @summary jdk.jfr.SettingControl conformance: white-box structure + black-box anonymous subclass.
 * @run main SettingControlTest
 */

import jdk.jfr.*;
import java.util.Set;
import java.lang.reflect.*;

public class SettingControlTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String n) { for (Method m : c.getMethods()) if (m.getName().equals(n)) return true; return false; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("jdk.jfr.SettingControl");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(Modifier.isAbstract(c.getModifiers()), "abstract");
        check(hasMethod(c, "combine"), "combine");
        check(hasMethod(c, "setValue"), "setValue");
        check(hasMethod(c, "getValue"), "getValue");
    }

    static void testBlackBox() {
        SettingControl sc = new SettingControl() {
            private String value = "";
            public String combine(Set<String> values) { return values.iterator().hasNext() ? values.iterator().next() : value; }
            public void setValue(String v) { value = v; }
            public String getValue() { return value; }
        };
        sc.setValue("5 s");
        check("5 s".equals(sc.getValue()), "getValue");
        check("5 s".equals(sc.combine(Set.of("5 s"))), "combine");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
