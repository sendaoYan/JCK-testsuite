/*
 * @test
 * @summary java.beans.DesignMode conformance: white-box structure + black-box behavior.
 * @run main DesignModeTest
 */

import java.beans.DesignMode;

public class DesignModeTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.beans.DesignMode");
        check(c.isInterface(), "is an interface");
        check("designTime".equals(DesignMode.PROPERTYNAME), "PROPERTYNAME constant");
    }

    static void testBlackBox() throws Exception {
        final boolean[] flag = new boolean[1];
        DesignMode dm = new DesignMode() {
            public void setDesignTime(boolean b) { flag[0] = b; }
            public boolean isDesignTime() { return flag[0]; }
        };
        dm.setDesignTime(true);
        check(dm.isDesignTime(), "set/isDesignTime");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
