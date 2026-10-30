/*
 * @test
 * @summary JLS 8.1.1.2: it is a compile-time error if a class declares a sealed
 *          or final direct superclass and is not permitted to extend it; final
 *          classes are not allowed to have subclasses.
 * @compile/fail ExtendFinalClass.java
 */

final class Base {}

class ExtendFinalClass extends Base {}   // error: cannot inherit from final Base
