/*
 * @test
 * @summary java.lang.annotation.Target negative: a FIELD-only annotation applied to a type is a
 *          compile-time error (javac: "annotation type/interface not applicable to this kind of
 *          declaration").
 * @compile/fail FieldOnlyOnType.java
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface FieldOnly3 {
}

@FieldOnly3
class FieldOnlyOnType {
}
