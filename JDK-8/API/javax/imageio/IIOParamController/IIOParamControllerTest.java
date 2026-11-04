/*
 * @test
 * @summary javax.imageio.IIOParamController conformance: interface hierarchy and methods.
 * @run main IIOParamControllerTest
 */

import java.lang.reflect.*;
import java.util.*;
import java.io.*;
import javax.imageio.IIOParam;
import javax.imageio.IIOParamController;
import javax.imageio.ImageReadParam;

public class IIOParamControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] a) throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParamController");
        check(c.isInterface(), "isInterface");
        check(Modifier.isPublic(c.getModifiers()), "public");
        check(c.getInterfaces().length == 0, "no direct superinterfaces");
        Set<String> dm = new HashSet<>(); for (Method m : c.getDeclaredMethods()) dm.add(m.getName());
        check(dm.contains("activate"), "method activate");
        testStructureExtras();
        testBehavior();
        System.out.println("PASS (" + passed + " assertions)");
    }

    // ---- additions (CORE-B): SAM structure ----
    static void testStructureExtras() throws Exception {
        Class<?> c = Class.forName("javax.imageio.IIOParamController");
        Method[] ms = c.getDeclaredMethods();
        check(ms.length == 1, "single abstract method");
        check(ms[0].getName().equals("activate"), "method activate");
        check(Modifier.isAbstract(ms[0].getModifiers()), "activate is abstract");
        check(Modifier.isPublic(ms[0].getModifiers()), "activate is public");
        check(ms[0].getReturnType().getName().equals("boolean"), "activate returns boolean");
        check(ms[0].getParameterTypes().length == 1, "activate takes one arg");
        check(ms[0].getParameterTypes()[0].getName().equals("javax.imageio.IIOParam"), "activate takes IIOParam");
        check(c.getDeclaredFields().length == 0, "no declared fields");
        check(c.getDeclaredClasses().length == 0, "no declared member types");
    }

    // ---- additions (CORE-B): black-box behavior ----
    // Exposes the protected defaultController field for quirk probing.
    static class ParamWithDefaultController extends ImageReadParam {
        ParamWithDefaultController(IIOParamController dc) {
            this.defaultController = dc;
        }
    }

    static void testBehavior() {
        // lambda forms: the SAM contract only fixes the boolean return
        IIOParamController reject = param -> false;
        IIOParamController accept = param -> true;
        check(!reject.activate(new ImageReadParam()), "rejecting controller returns false");
        check(accept.activate(new ImageReadParam()), "accepting controller returns true");

        // a controller observes the exact IIOParam instance it is handed
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
        check(p.hasController(), "hasController after setController");
        check(p.getController() != null, "getController non-null");
        check(!p.activateController(), "activateController returns controller's false");
        check(called[0], "activateController invoked the controller");

        // returning true is relayed unchanged
        p.setController(param -> true);
        check(p.activateController(), "activateController relays true");

        // activateController with no controller: javadoc says "false if none",
        // but both JDK-8 and JDK-27 actually throw IllegalStateException (probed)
        ImageReadParam bare = new ImageReadParam();
        check(!bare.hasController(), "no controller by default");
        check(bare.getController() == null, "getController null by default");
        boolean bareThrew = false;
        try { bare.activateController(); } catch (IllegalStateException e) { bareThrew = true; }
        check(bareThrew, "activateController throws ISE without controller (javadoc divergence)");
        check(bare.getDefaultController() == null, "defaultController null for plain param");

        // setController(null) detaches the controller
        p.setController(null);
        check(!p.hasController(), "setController(null) detaches");

        // a controller may mutate the param; mutations persist after activation
        final ImageReadParam mut = new ImageReadParam();
        mut.setController(new IIOParamController() {
            public boolean activate(IIOParam param) {
                param.setSourceSubsampling(2, 2, 0, 0);
                return true;
            }
        });
        check(mut.activateController(), "mutating controller returns true");
        check(mut.getSourceXSubsampling() == 2 && mut.getSourceYSubsampling() == 2, "controller mutations persist");

        // exceptions raised inside activate() propagate out of activateController unchanged
        final ImageReadParam boom = new ImageReadParam();
        boom.setController(new IIOParamController() {
            public boolean activate(IIOParam param) { throw new IllegalStateException("coreb-relay"); }
        });
        boolean relayed = false;
        try { boom.activateController(); } catch (IllegalStateException e) {
            relayed = "coreb-relay".equals(e.getMessage());
        }
        check(relayed, "controller exceptions relayed with message");

        // quirk (probed): defaultController is never consulted by activateController;
        // it stays informational unless explicitly installed with setController
        final boolean[] defaultSeen = new boolean[1];
        ParamWithDefaultController pd = new ParamWithDefaultController(new IIOParamController() {
            public boolean activate(IIOParam param) { defaultSeen[0] = true; return true; }
        });
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
}
