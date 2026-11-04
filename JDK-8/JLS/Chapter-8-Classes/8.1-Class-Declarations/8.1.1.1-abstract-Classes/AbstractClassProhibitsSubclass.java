/*
 * @test
 * @summary JLS 8.1.1.1 (Java SE 8): it is a compile-time error to declare an abstract class
 *          such that no subclass could implement all its abstract methods (two
 *          abstract methods with the same signature but incompatible returns).
 * @compile/fail AbstractClassProhibitsSubclass.java
 */
interface Colorable { void setColor(int color); }
abstract class AbstractClassProhibitsSubclass implements Colorable {
    public abstract int setColor(int color);   // error: incompatible return type
}
