/*
 * @test
 * @summary JLS 9.6.1: the return type of an annotation interface element may not
 *          be an array type whose component type is itself an array.
 * @compile/fail AnnotationElementInvalidReturnType.java
 */

@interface AnnotationElementInvalidReturnType {
    String[][] value();   // error: nested array type not permitted
}
