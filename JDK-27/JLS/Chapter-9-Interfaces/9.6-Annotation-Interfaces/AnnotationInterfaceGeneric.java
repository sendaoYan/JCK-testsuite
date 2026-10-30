/*
 * @test
 * @summary JLS 9.6: an annotation interface is never generic; its declaration may
 *          not declare type variables.
 * @compile/fail AnnotationInterfaceGeneric.java
 */

@interface AnnotationInterfaceGeneric<T> {}   // error: annotation interface cannot be generic
