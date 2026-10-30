/*
 * @test
 * @summary JLS 8.1.1.2 (Java SE 8): a final class cannot be extended.
 * @compile/fail FinalClassSubclass.java
 */

final class Base {}

class FinalClassSubclass extends Base {}   // error: cannot inherit from final Base
