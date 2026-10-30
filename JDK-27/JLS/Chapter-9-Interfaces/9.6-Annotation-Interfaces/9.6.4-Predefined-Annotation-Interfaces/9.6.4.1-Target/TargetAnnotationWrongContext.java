/*
 * @test
 * @summary JLS 9.6.4.1: the Target meta-annotation restricts the contexts in
 *          which an annotation interface may apply; applying it to a
 *          non-conforming context is a compile-time error.
 * @compile/fail TargetAnnotationWrongContext.java
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@interface OnlyOnMethods {}

@OnlyOnMethods   // error: not applicable to type
class TargetAnnotationWrongContext {}
