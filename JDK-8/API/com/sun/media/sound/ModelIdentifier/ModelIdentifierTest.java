/*
 * @test
 * @summary com.sun.media.sound.ModelIdentifier conformance: object/variable/instance data holder.
 * @run main ModelIdentifierTest
 */

import com.sun.media.sound.ModelIdentifier;

public class ModelIdentifierTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) {
        ModelIdentifier mi = new ModelIdentifier("obj", "var", 5);
        check("obj".equals(mi.getObject()), "getObject");
        check("var".equals(mi.getVariable()), "getVariable");
        check(mi.getInstance() == 5, "getInstance");

        mi.setInstance(9);
        mi.setObject("o2");
        mi.setVariable("v2");
        check(mi.getInstance() == 9, "setInstance");
        check("o2".equals(mi.getObject()), "setObject");
        check("v2".equals(mi.getVariable()), "setVariable");

        check(mi.equals(new ModelIdentifier("o2", "v2", 9)), "equals");
        check(!mi.equals(new ModelIdentifier("o2", "v2", 8)), "not equals");
        check(mi.hashCode() == new ModelIdentifier("o2", "v2", 9).hashCode(), "hashCode");
        check(mi.toString() != null, "toString");

        ModelIdentifier one = new ModelIdentifier("x");
        check("x".equals(one.getObject()) && one.getInstance() == 0, "one-arg ctor");

        System.out.println("PASS (" + passed + " assertions)");
    }
}
