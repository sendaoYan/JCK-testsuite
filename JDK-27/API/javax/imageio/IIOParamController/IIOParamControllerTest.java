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

        // ---- additions (CORE-B) ----
        testCoreBStructure();
        testCoreBControllerPlumbing();
    }

    static void testCoreBStructure() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParamController");
        Method m = c.getDeclaredMethods()[0];
        check(java.lang.reflect.Modifier.isAbstract(m.getModifiers()), "activate is abstract");
        check(java.lang.reflect.Modifier.isPublic(m.getModifiers()), "activate is public");
        check(m.getExceptionTypes().length == 0, "activate declares no checked exceptions");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredClasses().length == 0, "no declared member types");
    }

    static class ParamWithDefaultController extends ImageReadParam {
        ParamWithDefaultController(IIOParamController dc) {
            this.defaultController = dc;
        }
    }

    static void testCoreBControllerPlumbing() {
        // no controller: javadoc says "false if none", but both JDKs throw ISE (probed)
        ImageReadParam bare = new ImageReadParam();
        check(!bare.hasController(), "no controller by default");
        check(bare.getController() == null, "getController null by default");
        boolean bareThrew = false;
        try { bare.activateController(); } catch (IllegalStateException e) { bareThrew = true; }
        check(bareThrew, "activateController throws ISE without controller (javadoc divergence)");
        check(bare.getDefaultController() == null, "defaultController null for plain param");

        // returning true is relayed; setController(null) detaches
        ImageReadParam p = new ImageReadParam();
        p.setController(param -> true);
        check(p.hasController() && p.getController() != null, "controller attached");
        check(p.activateController(), "activateController relays true");
        p.setController(null);
        check(!p.hasController(), "setController(null) detaches");

        // a controller may mutate the param; mutations persist after activation
        ImageReadParam mut = new ImageReadParam();
        mut.setController(param -> {
            param.setSourceSubsampling(2, 2, 0, 0);
            return true;
        });
        check(mut.activateController(), "mutating controller returns true");
        check(mut.getSourceXSubsampling() == 2 && mut.getSourceYSubsampling() == 2, "controller mutations persist");

        // exceptions raised inside activate() propagate out of activateController unchanged
        ImageReadParam boom = new ImageReadParam();
        boom.setController(param -> { throw new IllegalStateException("coreb-relay"); });
        boolean relayed = false;
        try { boom.activateController(); } catch (IllegalStateException e) {
            relayed = "coreb-relay".equals(e.getMessage());
        }
        check(relayed, "controller exceptions relayed with message");

        // quirk (probed): defaultController is never consulted by activateController
        final boolean[] defaultSeen = new boolean[1];
        ParamWithDefaultController pd = new ParamWithDefaultController(param -> { defaultSeen[0] = true; return true; });
        check(pd.getDefaultController() != null, "getDefaultController exposes protected field");
        check(!pd.hasController(), "defaultController does not count as controller");
        boolean pdThrew = false;
        try { pd.activateController(); } catch (IllegalStateException e) { pdThrew = true; }
        check(pdThrew, "activateController throws ISE with only defaultController");
        check(!defaultSeen[0], "defaultController never activated implicitly");
        pd.setController(pd.getDefaultController());
        check(pd.hasController(), "explicit set of default controller");
        check(pd.activateController() && defaultSeen[0], "explicit default controller runs");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
