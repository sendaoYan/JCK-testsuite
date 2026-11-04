/*
 * @test
 * @summary java.awt.event.ContainerEvent conformance: white-box structure + black-box behavior.
 * @run main ContainerEventTest
 */

import java.awt.Component;
import java.awt.Container;
import java.awt.event.ComponentEvent;
import java.awt.event.ContainerEvent;
import java.lang.reflect.Modifier;

public class ContainerEventTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.awt.event.ContainerEvent");
        int m = c.getModifiers();
        check(Modifier.isPublic(m), "ContainerEvent is public");
        check(!Modifier.isAbstract(m), "ContainerEvent is concrete");
        check(c.getSuperclass() == ComponentEvent.class, "ContainerEvent extends ComponentEvent");
        check(c.getField("COMPONENT_ADDED").getType() == int.class, "COMPONENT_ADDED int");
        check(c.getField("COMPONENT_REMOVED").getType() == int.class, "COMPONENT_REMOVED int");
        check(c.getConstructor(Component.class, int.class, Component.class) != null, "has 3-arg ctor");
    }

    static void testBlackBox() throws Exception {
        Container cont = new Container();
        Component child = new Component() { };
        child.setName("kid");
        ContainerEvent e = new ContainerEvent(cont, ContainerEvent.COMPONENT_ADDED, child);

        check(e.getID() == ContainerEvent.COMPONENT_ADDED, "getID round-trips");
        check(e.getSource() == cont, "getSource round-trips");
        check(e.getContainer() == cont, "getContainer returns source");
        check(e.getChild() == child, "getChild round-trips");

        // documented constants
        check(ContainerEvent.CONTAINER_FIRST == 300, "CONTAINER_FIRST == 300");
        check(ContainerEvent.CONTAINER_LAST == 301, "CONTAINER_LAST == 301");
        check(ContainerEvent.COMPONENT_ADDED == 300, "COMPONENT_ADDED == 300");
        check(ContainerEvent.COMPONENT_REMOVED == 301, "COMPONENT_REMOVED == 301");

        // paramString
        check(e.paramString().equals("COMPONENT_ADDED,child=kid"), "paramString COMPONENT_ADDED");
        ContainerEvent r = new ContainerEvent(cont, ContainerEvent.COMPONENT_REMOVED, child);
        check(r.paramString().equals("COMPONENT_REMOVED,child=kid"), "paramString COMPONENT_REMOVED");
        ContainerEvent unk = new ContainerEvent(cont, 9999, child);
        check(unk.paramString().equals("unknown type,child=kid"), "paramString unknown type");

        // inheritance: ContainerEvent IS-A ComponentEvent and AWTEvent
        check(e instanceof ComponentEvent, "ContainerEvent instanceof ComponentEvent");
        check(e.getComponent() == cont, "getComponent returns source container");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
