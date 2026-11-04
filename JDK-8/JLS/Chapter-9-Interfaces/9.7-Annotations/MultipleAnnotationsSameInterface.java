/*
 * @test
 * @summary JLS 9.7.5 (Java SE 8): applying the same annotation interface more than
 *          once is a compile-time error unless the interface is repeatable.
 * @compile/fail MultipleAnnotationsSameInterface.java
 */

@interface A {}

@A @A
class MultipleAnnotationsSameInterface {   // error: A is not a repeatable annotation type
}
