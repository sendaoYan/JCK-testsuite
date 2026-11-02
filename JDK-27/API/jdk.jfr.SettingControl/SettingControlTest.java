/*
 * @test
 * @summary jdk.jfr.SettingControl conformance: anonymous subclass set/combine/get.
 * @run main SettingControlTest
 */

import jdk.jfr.*;
import java.util.Set;

public class SettingControlTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        SettingControl sc = new SettingControl() {
            private String value = "";
            public String combine(Set<String> values) { return values.iterator().hasNext() ? values.iterator().next() : value; }
            public void setValue(String v) { value = v; }
            public String getValue() { return value; }
        };
        sc.setValue("5 s");
        check("5 s".equals(sc.getValue()), "getValue");
        check("5 s".equals(sc.combine(Set.of("5 s"))), "combine");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
