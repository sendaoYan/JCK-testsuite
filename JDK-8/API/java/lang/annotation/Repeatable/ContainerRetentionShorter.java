/*
 * @test
 * @summary java.lang.annotation.Repeatable negative: a containing annotation type whose retention
 *          is shorter than the repeatable annotation type is a compile-time error (javac:
 *          "retention of containing annotation type (...) is shorter than ...").
 * @compile/fail ContainerRetentionShorter.java
 */

import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.CLASS)
@interface Container {
    Rep[] value();
}

@Retention(RetentionPolicy.RUNTIME)
@Repeatable(Container.class)
@interface Rep {
}

class ContainerRetentionShorter {
    @Rep
    @Rep
    int f;
}
