/*
 * @test
 * @summary JLS 6.3 Example 6.3-1: the scope of a top level class declaration
 *          includes all class and interface declarations of the package, so a
 *          class type need not be declared before it is used.
 * @run main ClassDeclarationsBeforeUse
 */

class Node {
    int x;
    Node next;        // reference to Node itself
    PointList list;   // use of PointList before its textual declaration
}

class PointList {
    Node first;
}

public class ClassDeclarationsBeforeUse {
    public static void main(String[] args) {
        Node n = new Node();
        n.x = 5;
        n.next = new Node();
        n.list = new PointList();
        n.list.first = n.next;

        if (n.list.first != n.next) throw new AssertionError("forward reference");
        System.out.println("PASS");
    }
}
