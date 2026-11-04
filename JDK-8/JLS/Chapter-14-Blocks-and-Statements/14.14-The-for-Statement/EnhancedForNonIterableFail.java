/*
 * @test
 * @summary JLS 14.14.2: the expression of an enhanced for statement must be an
 *          Iterable or an array type; otherwise it is a compile-time error.
 * @compile/fail EnhancedForNonIterableFail.java
 */

class EnhancedForNonIterableFail {
    void m(Object o) {
        for (Object x : o) {   // error: for-each not applicable to type Object
        }
    }
}
