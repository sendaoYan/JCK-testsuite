/*
 * @test
 * @summary JLS 15.20.2 (Java SE 8): the relational expression instanceof
 *          requires the reference type to be castable to the target type;
 *          otherwise it is a compile-time error.
 * @compile/fail InstanceofIncompatible.java
 */

class InstanceofIncompatible {
    void test(String s) {
        boolean b = s instanceof Integer;   // error: incompatible types: String cannot be converted to Integer
    }
}
