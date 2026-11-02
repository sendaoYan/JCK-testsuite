/*
 * @test
 * @summary java.rmi.MarshalledObject conformance: get returns original, equals.
 * @run main MarshalledObjectTest
 */

import java.rmi.*;
public class MarshalledObjectTest {
    static int passed;
    static void check(boolean c, String m) { if (!c) throw new AssertionError(m); passed++; }
    public static void main(String[] args) throws Exception {
        MarshalledObject<String> mo = new MarshalledObject<String>("hello");
        check("hello".equals(mo.get()), "get");
        check(mo.equals(new MarshalledObject<String>("hello")), "equals");
        check(!mo.equals(new MarshalledObject<String>("world")), "not equals");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
