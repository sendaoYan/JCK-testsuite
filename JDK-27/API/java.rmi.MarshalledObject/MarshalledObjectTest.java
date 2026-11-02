/*
 * @test
 * @summary java.rmi.MarshalledObject conformance: marshalling round trip and equality.
 * @run main MarshalledObjectTest
 */

import java.rmi.MarshalledObject;

public class MarshalledObjectTest {
    static int passed;
    static void check(boolean cond, String msg) { if (!cond) throw new AssertionError(msg); passed++; }
    public static void main(String[] args) throws Exception {
        MarshalledObject<String> mo = new MarshalledObject<>("hello");
        check("hello".equals(mo.get()), "get");
        check(mo.hashCode() == mo.hashCode(), "hashcode stable");
        MarshalledObject<String> mo2 = new MarshalledObject<>("hello");
        check(mo.equals(mo2), "equals same content");
        check(!mo.equals(new MarshalledObject<>("world")), "not equals different");
        check(!mo.equals(null), "not equals null");
        System.out.println("PASS (" + passed + " assertions)");
    }
}
