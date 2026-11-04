/*
 * @test
 * @summary java.lang.annotation.Repeatable negative: a containing annotation type applicable to
 *          fewer targets than the repeatable annotation type is a compile-time error at the use
 *          site (javac: "containing annotation type ... applicable to more targets ...").
 * @compile/fail ContainerTargetNotApplicable.java
 */

import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@interface Container2 {
    Rep2[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Container2.class)
@interface Rep2 {
}

class ContainerTargetNotApplicable {
    @Rep2
    @Rep2
    int f;
}
