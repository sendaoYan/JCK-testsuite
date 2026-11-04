/*
 * @test
 * @summary java.lang.annotation.Repeatable negative: repeating a non-repeatable annotation type
 *          is a compile-time error (javac: "... is not a repeatable annotation type/interface").
 * @compile/fail NonRepeatableRepeated.java
 */

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@interface Plain {
}

@Plain
@Plain
class NonRepeatableRepeated {
}
