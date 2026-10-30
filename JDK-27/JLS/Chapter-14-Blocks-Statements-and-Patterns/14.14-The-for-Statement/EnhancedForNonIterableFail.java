/*
 * @test
 * @summary JLS 14.14.2: the expression of an enhanced for statement must have an
 *          array type or a subtype of Iterable.
 * @compile/fail EnhancedForNonIterableFail.java
 */

class EnhancedForNonIterableFail {
    void m(Object o) {
        for (Object x : o) {}   // error: Object is not Iterable or an array
    }
}
