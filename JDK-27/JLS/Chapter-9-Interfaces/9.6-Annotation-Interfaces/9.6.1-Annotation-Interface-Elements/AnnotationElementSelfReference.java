/*
 * @test
 * @summary JLS 9.6.1: it is a compile-time error if an annotation interface
 *          contains an element of its own type, directly or indirectly.
 * @compile/fail AnnotationElementSelfReference.java
 */

@interface AnnotationElementSelfReference {
    AnnotationElementSelfReference value();   // error: circular
}
