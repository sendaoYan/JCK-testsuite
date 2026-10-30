/*
 * @test
 * @summary JLS 9.6.4.7: SafeVarargs on a static generic varargs method
 *          suppresses heap-pollution warnings.
 * @compile SafeVarargsMethod.java
 */

import java.util.Arrays;
import java.util.List;

class SafeVarargsMethod {
    @SafeVarargs
    static <T> List<T> listOf(T... elements) {
        return Arrays.asList(elements);
    }
}
