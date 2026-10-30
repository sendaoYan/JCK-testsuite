/*
 * @test
 * @summary JLS 8.3: a class may inherit two fields with the same name, but any
 *          attempt to refer to such a field by simple name is ambiguous and is a
 *          compile-time error.
 * @compile/fail MultiplyInheritedFieldAmbiguous.java
 */

interface Frob { float v = 2.0f; }
class SuperTest { int v = 3; }

class MultiplyInheritedFieldAmbiguous extends SuperTest implements Frob {
    void printV() { System.out.println(v); }   // error: reference to v is ambiguous
}
