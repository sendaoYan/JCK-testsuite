/*
 * @test
 * @summary javax.imageio.IIOParamController conformance: white-box structure + black-box behavior.
 * @run main IIOParamControllerTest
 */

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import javax.imageio.IIOParam;
import javax.imageio.IIOParamController;
import javax.imageio.ImageReadParam;

public class IIOParamControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParamController");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "single abstract method");
        check(ms[0].getName().equals("activate"), "method activate");
        check(ms[0].getReturnType().getName().equals("boolean"), "activate returns boolean");
        check(ms[0].getParameterTypes().length == 1, "activate takes one arg");
        check(ms[0].getParameterTypes()[0].getName().equals("javax.imageio.IIOParam"), "activate takes IIOParam");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() throws Exception {
        // functional-interface lambda forms: an implementation may accept or reject
        IIOParamController reject = param -> false;
        IIOParamController accept = param -> true;

        check(!reject.activate(new ImageReadParam()), "rejecting controller returns false");
        check(accept.activate(new ImageReadParam()), "accepting controller returns true");

        // a controller observes the exact IIOParam it is handed
        final IIOParam[] observed = new IIOParam[1];
        IIOParamController recorder = new IIOParamController() {
            public boolean activate(IIOParam param) { observed[0] = param; return true; }
        };
        ImageReadParam p = new ImageReadParam();
        check(recorder.activate(p), "recorder returns true");
        check(observed[0] == p, "recorder observed the passed param");

        // end-to-end: IIOParam.setController + activateController dispatches to the controller
        final boolean[] called = new boolean[1];
        p.setController(new IIOParamController() { public boolean activate(IIOParam param) { called[0] = true; return false; } });
        check(!p.activateController(), "activateController returns controller's false");
        check(called[0], "activateController invoked the controller");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
