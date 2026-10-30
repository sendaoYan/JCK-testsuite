/*
 * @test
 * @summary JLS 8.1.5: a single method declaration in a class may implement
 *          methods of more than one superinterface.
 * @run main SingleMethodImplementsTwoInterfaces
 */

interface Fish  { int getNumberOfScales(); }
interface Piano { int getNumberOfScales(); }

class Tuna implements Fish, Piano {
    public int getNumberOfScales() { return 91; }
}

public class SingleMethodImplementsTwoInterfaces {
    public static void main(String[] args) {
        Tuna t = new Tuna();
        Fish f = t;
        Piano p = t;
        if (f.getNumberOfScales() != 91 || p.getNumberOfScales() != 91)
            throw new AssertionError("single method implements both interfaces");
        System.out.println("PASS");
    }
}
