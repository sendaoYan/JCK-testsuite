/*
 * @test
 * @summary JLS 9.6 (Java SE 8): an annotation interface declaration may not be
 *          generic.
 * @compile/fail AnnotationInterfaceCannotBeGeneric.java
 */

@interface AnnotationInterfaceCannotBeGeneric<T> {   // error: annotation type cannot be generic
    T value();
}
