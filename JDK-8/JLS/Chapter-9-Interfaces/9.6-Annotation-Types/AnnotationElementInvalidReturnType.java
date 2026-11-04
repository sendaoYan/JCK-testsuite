/*
 * @test
 * @summary JLS 9.6.1 (Java SE 8): an annotation interface element may not have a
 *          return type that is a reference type other than String, Class, an enum,
 *          or an annotation interface.
 * @compile/fail AnnotationElementInvalidReturnType.java
 */

@interface AnnotationElementInvalidReturnType {
    java.util.List m();   // error: invalid type for annotation interface element
}
