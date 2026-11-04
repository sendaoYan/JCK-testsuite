/*
 * @test
 * @summary java.awt.image.renderable.ParameterBlock conformance: class reflection.
 * @run main ParameterBlockTest
 */

public class ParameterBlockTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static boolean hasMethod(Class<?> c, String name) {
        for (java.lang.reflect.Method m : c.getMethods())
            if (m.getName().equals(name)) return true;
        return false;
    }

    public static void main(String[] args) throws Exception {
        Class<?> c = Class.forName("java.awt.image.renderable.ParameterBlock");
        check(!c.isInterface() && !c.isEnum(), "is class");
        check(c.getSuperclass().getName().equals("java.lang.Object"), "superclass java.lang.Object");
        java.util.Set<String> ifaces = new java.util.HashSet<String>();
        for (Class<?> i : c.getInterfaces()) ifaces.add(i.getName());
        String[] expected = {"java.io.Serializable", "java.lang.Cloneable"};
        check(ifaces.equals(new java.util.HashSet<String>(java.util.Arrays.asList(expected))), "interfaces");
        check(hasMethod(c, "shallowClone"), "method shallowClone");
        check(hasMethod(c, "clone"), "method clone");
        check(hasMethod(c, "addSource"), "method addSource");
        check(hasMethod(c, "getSource"), "method getSource");
        check(hasMethod(c, "setSource"), "method setSource");
        check(hasMethod(c, "getRenderedSource"), "method getRenderedSource");
        check(hasMethod(c, "getRenderableSource"), "method getRenderableSource");
        check(hasMethod(c, "getNumSources"), "method getNumSources");
        check(hasMethod(c, "getSources"), "method getSources");
        check(hasMethod(c, "setSources"), "method setSources");
        check(hasMethod(c, "removeSources"), "method removeSources");
        check(hasMethod(c, "getNumParameters"), "method getNumParameters");
        check(hasMethod(c, "getParameters"), "method getParameters");
        check(hasMethod(c, "setParameters"), "method setParameters");
        check(hasMethod(c, "removeParameters"), "method removeParameters");
        check(hasMethod(c, "add"), "method add");
        check(hasMethod(c, "set"), "method set");
        check(hasMethod(c, "getObjectParameter"), "method getObjectParameter");
        check(hasMethod(c, "getByteParameter"), "method getByteParameter");
        check(hasMethod(c, "getCharParameter"), "method getCharParameter");
        check(hasMethod(c, "getShortParameter"), "method getShortParameter");
        check(hasMethod(c, "getIntParameter"), "method getIntParameter");
        check(hasMethod(c, "getLongParameter"), "method getLongParameter");
        check(hasMethod(c, "getFloatParameter"), "method getFloatParameter");
        check(hasMethod(c, "getDoubleParameter"), "method getDoubleParameter");
        check(hasMethod(c, "getParamClasses"), "method getParamClasses");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
