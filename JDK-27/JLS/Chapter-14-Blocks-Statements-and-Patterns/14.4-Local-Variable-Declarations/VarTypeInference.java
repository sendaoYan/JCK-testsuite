/*
 * @test
 * @summary JLS 14.4 / 14.4.1: a local variable declared with var takes the
 *          (upward-projected) type of its initializer.
 * @run main VarTypeInference
 */

import java.util.List;

public class VarTypeInference {
    public static void main(String[] args) {
        var a = 1;                          // int
        int ai = a;
        if (ai != 1) throw new AssertionError("var int");

        var b = List.of(1, 2);              // List<Integer>
        List<Integer> bl = b;
        if (bl.size() != 2 || bl.get(0) != 1) throw new AssertionError("var List");

        var c = "x".getClass();             // Class<? extends String>
        Class<? extends String> cc = c;
        if (!"java.lang.String".equals(cc.getName()))
            throw new AssertionError("var Class: " + cc.getName());

        var d = new Object() {};            // anonymous class type
        Object dd = d;
        if (!dd.getClass().getName().startsWith("VarTypeInference$"))
            throw new AssertionError("var anonymous: " + dd.getClass().getName());

        var e = List.of("a", "b");          // upward projection -> List<String>
        List<String> el = e;
        if (!"a".equals(el.get(0))) throw new AssertionError("var upward projection");

        System.out.println("PASS");
    }
}
