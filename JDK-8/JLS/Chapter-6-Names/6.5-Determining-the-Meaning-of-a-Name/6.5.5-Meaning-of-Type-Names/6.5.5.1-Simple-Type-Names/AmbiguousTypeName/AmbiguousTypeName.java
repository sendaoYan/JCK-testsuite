/*
 * @test
 * @summary JLS 6.5.5.1 (Java SE 8): it is a compile-time error if a simple type
 *          name is in the scope of more than one visible declaration of a type
 *          with that name (here two single-type imports).
 * @compile amb1/T.java amb2/T.java
 * @compile/fail AmbiguousTypeName.java
 */

import amb1.T;
import amb2.T;   // error: T is already defined in this compilation unit

class AmbiguousTypeName {
    T t;
}
