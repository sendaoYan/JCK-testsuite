/*
 * @test
 * @summary JLS 8.1.2: a generic class declaration defines a set of parameterized
 *          types that share the same class at run time.
 * @run main GenericClassSharedRuntimeClass
 */

import java.util.Vector;

public class GenericClassSharedRuntimeClass {
    public static void main(String[] args) {
        Vector<String>  x = new Vector<String>();
        Vector<Integer> y = new Vector<Integer>();
        boolean b = x.getClass() == y.getClass();
        if (!b) throw new AssertionError("parameterized types must share one class");
        System.out.println("PASS");
    }
}
