/*
 * @test
 * @summary java.lang.ModuleLayer.Controller conformance: white-box structure + black-box behavior.
 * @run main ControllerTest
 */
import java.lang.ModuleLayer;
import java.lang.module.Configuration;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.List;

public class ControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = ModuleLayer.Controller.class;
        check(Modifier.isFinal(c.getModifiers()), "final");
        check(c.getConstructors().length == 0, "no public constructor");
        check(c.getMethod("layer").getReturnType() == ModuleLayer.class, "layer() returns ModuleLayer");
        check(c.getMethod("addReads", Module.class, Module.class).getReturnType() == ModuleLayer.Controller.class,
              "addReads returns Controller");
        check(c.getMethod("addExports", Module.class, String.class, Module.class).getReturnType() == ModuleLayer.Controller.class,
              "addExports returns Controller");
        check(c.getMethod("addOpens", Module.class, String.class, Module.class).getReturnType() == ModuleLayer.Controller.class,
              "addOpens returns Controller");
        check(c.getMethod("enableNativeAccess", Module.class).getReturnType() == ModuleLayer.Controller.class,
              "enableNativeAccess returns Controller");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testEmptyLayerController() {
        // An empty configuration has no parents, so the parent-layer list must be empty.
        ModuleLayer.Controller ctrl = ModuleLayer.defineModulesWithOneLoader(
                Configuration.empty(), List.of(), ClassLoader.getSystemClassLoader());
        check(ctrl != null, "defineModulesWithOneLoader returns a Controller");
        ModuleLayer layer = ctrl.layer();
        check(layer != null, "layer() non-null");
        check(layer.modules().isEmpty(), "controlled empty layer has no modules");
        check(layer.parents().isEmpty(), "empty layer has no parents");
        check(layer.configuration() == Configuration.empty(),
              "controlled layer reports the given configuration");
    }

    static void testForeignModuleRejected() {
        ModuleLayer.Controller ctrl = ModuleLayer.defineModulesWithOneLoader(
                Configuration.empty(), List.of(), ClassLoader.getSystemClassLoader());
        Module foreign = String.class.getModule();
        try {
            ctrl.addReads(foreign, foreign);
            throw new AssertionError("addReads on a module outside the layer: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
        try {
            ctrl.enableNativeAccess(foreign);
            throw new AssertionError("enableNativeAccess on a module outside the layer: expected IllegalArgumentException");
        } catch (IllegalArgumentException expected) {
            passed++;
        }
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testEmptyLayerController();
        testForeignModuleRejected();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
