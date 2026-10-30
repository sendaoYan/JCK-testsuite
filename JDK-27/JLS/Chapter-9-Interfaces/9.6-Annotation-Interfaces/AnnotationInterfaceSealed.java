/*
 * @test
 * @summary JLS 9.6: it is a compile-time error if an annotation interface
 *          declaration has the modifier sealed or non-sealed.
 * @compile/fail AnnotationInterfaceSealed.java
 */

sealed @interface AnnotationInterfaceSealed {}   // error: sealed not allowed here
