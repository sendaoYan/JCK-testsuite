/*
 * @test
 * @summary JLS 14.4.1 / 18: the type of a local variable declared with var is
 *          inferred from its initializer.
 * @run main VarLocalVariableInference
 */

import java.util.ArrayList;

public class VarLocalVariableInference {
    public static void main(String[] args) {
        var x = 42;                          // inferred int
        if (x != 42) throw new AssertionError("var int");

        var list = new ArrayList<String>();  // inferred ArrayList<String>
        list.add("a");
        if (!(list instanceof ArrayList)) throw new AssertionError("var ArrayList");
        if (!"a".equals(list.get(0))) throw new AssertionError("var list element");

        var s = "str";                       // inferred String
        if (!(s instanceof String)) throw new AssertionError("var String");

        System.out.println("PASS");
    }
}
