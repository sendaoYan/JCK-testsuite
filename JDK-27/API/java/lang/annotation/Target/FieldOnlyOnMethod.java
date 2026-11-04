/*
 * @test
 * @summary java.lang.annotation.Target negative: a FIELD-only annotation applied to a method is a
 *          compile-time error (javac: "annotation type/interface not applicable to this kind of
 *          declaration").
 * @compile/fail FieldOnlyOnMethod.java
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@interface FieldOnly {
}

class FieldOnlyOnMethod {
    @FieldOnly
    void work() {
    }
}
