/*
 * @test
 * @summary java.rmi.activation.ActivationGroupDesc.CommandEnvironment conformance: concrete class reflection.
 * @run main CommandEnvironmentTest
 */

public class CommandEnvironmentTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getDeclaredMethods()) if (m.getName().equals(name)) return true;
        return false;
    }
    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.rmi.activation.ActivationGroupDesc$CommandEnvironment");
        check(!c.isInterface(), "not an interface");
        check(!java.lang.reflect.Modifier.isAbstract(c.getModifiers()), "concrete");
        check(Class.forName("java.io.Serializable").isAssignableFrom(c), "implements/extends java.io.Serializable");
        check(hasMethod(c, "equals"), "declares equals");
        check(hasMethod(c, "getCommandOptions"), "declares getCommandOptions");
        check(hasMethod(c, "getCommandPath"), "declares getCommandPath");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
