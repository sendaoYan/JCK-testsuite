/*
 * @test
 * @summary java.lang.ModuleLayer.Controller conformance: final class reflection.
 * @run main ControllerTest
 */
import java.lang.ModuleLayer;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

public class ControllerTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }

    public static void main(String[] args) {
        Class<?> c = ModuleLayer.Controller.class;
        check(Modifier.isFinal(c.getModifiers()), "final");
        java.util.Set<String> names = new java.util.HashSet<String>();
        for (Method m : c.getMethods()) names.add(m.getName());
        check(names.contains("layer"), "layer()");
        check(names.contains("addReads"), "addReads()");
        check(names.contains("addExports"), "addExports()");
        check(names.contains("addOpens"), "addOpens()");
        check(names.contains("enableNativeAccess"), "enableNativeAccess()");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
