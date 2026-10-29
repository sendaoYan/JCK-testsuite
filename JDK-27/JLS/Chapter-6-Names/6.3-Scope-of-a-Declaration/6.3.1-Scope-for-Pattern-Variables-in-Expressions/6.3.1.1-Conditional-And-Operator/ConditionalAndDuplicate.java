/*
 * @test
 * @summary JLS 6.3.1.1: it is a compile-time error if both operands of && declare
 *          a pattern variable of the same name (both introduced when true).
 * @compile/fail ConditionalAndDuplicate.java
 */

class ConditionalAndDuplicate {
    void m(Object a, Object b) {
        boolean r = (a instanceof String s) && (b instanceof String s);   // error
    }
}
