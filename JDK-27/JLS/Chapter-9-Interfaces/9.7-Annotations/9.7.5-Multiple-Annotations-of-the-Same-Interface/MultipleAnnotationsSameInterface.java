/*
 * @test
 * @summary JLS 9.7.5: it is a compile-time error if the same annotation type
 *          appears more than once at a single declaration, unless it is
 *          repeatable.
 * @compile/fail MultipleAnnotationsSameInterface.java
 */

@interface Single {}

@Single @Single   // error: duplicate annotation of non-repeatable type
class MultipleAnnotationsSameInterface {}
