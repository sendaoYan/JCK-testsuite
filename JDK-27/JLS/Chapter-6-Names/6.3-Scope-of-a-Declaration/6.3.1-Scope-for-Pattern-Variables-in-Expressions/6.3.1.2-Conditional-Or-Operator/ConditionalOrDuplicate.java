/*
 * @test
 * @summary JLS 6.3.1.2: it is a compile-time error if both operands of || declare
 *          a pattern variable of the same name.
 * @compile/fail ConditionalOrDuplicate.java
 */

class ConditionalOrDuplicate {
    void m(Object a, Object b) {
        boolean r = (a instanceof String s) || (b instanceof String s);   // error
    }
}
