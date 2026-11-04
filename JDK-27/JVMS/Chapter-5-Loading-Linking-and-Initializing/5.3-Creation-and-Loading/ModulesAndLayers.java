/*
 * @test
 * @summary JVMS 5.3.6: each class belongs to a module; the platform's core
 *          classes belong to the java.base module in the boot layer, while a
 *          class-path test class belongs to the unnamed module.
 * @run main ModulesAndLayers
 */

public class ModulesAndLayers {
    static int passed = 0;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    public static void main(String[] args) {
        Module base = Object.class.getModule();
        check(base != null && "java.base".equals(base.getName()),
                "Object belongs to module java.base");
        check(String.class.getModule() == base,
                "String belongs to the same java.base module");
        check(base.getLayer() != null,
                "java.base is present in the boot layer");

        Module mine = ModulesAndLayers.class.getModule();
        check(mine != null && !mine.isNamed(),
                "a class-path class belongs to the unnamed module");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
