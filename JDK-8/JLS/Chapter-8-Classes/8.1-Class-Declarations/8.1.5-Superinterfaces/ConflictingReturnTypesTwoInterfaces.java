/*
 * @test
 * @summary JLS 8.1.5 (Java SE 8): a single class cannot implement two interfaces declaring
 *          the same signature with incompatible return types.
 * @compile/fail ConflictingReturnTypesTwoInterfaces.java
 */
interface Fish { int getNumberOfScales(); }
interface StringBass { double getNumberOfScales(); }
class ConflictingReturnTypesTwoInterfaces implements Fish, StringBass {
    public int getNumberOfScales() { return 91; }   // error: incompatible return types
}
