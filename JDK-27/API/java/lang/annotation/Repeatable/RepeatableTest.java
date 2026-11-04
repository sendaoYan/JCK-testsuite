/*
 * @test
 * @summary java.lang.annotation.Repeatable conformance: white-box structure + black-box behavior.
 * @run main RepeatableTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Modifier;

public class RepeatableTest {
    static int passed;

    static void check(boolean cond, String msg) {
        if (!cond) throw new AssertionError(msg);
        passed++;
    }

    // ---- fixtures ----
    @Retention(RetentionPolicy.RUNTIME)
    @interface Schedules {
        Schedule[] value();
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Repeatable(Schedules.class)
    @interface Schedule {
        String cron();

        int order() default 0;
    }

    @Schedule(cron = "0 0 * * *", order = 1)
    @Schedule(cron = "0 30 6 * *", order = 2)
    static class Job {
    }

    @Schedule(cron = "@once")
    static class Single {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Repeatable");
        check(c.isAnnotation(), "Repeatable is an annotation type");
        check(c.isAnnotationPresent(Retention.class)
              && c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
        check(c.getDeclaredMethods().length == 1, "single member");
        check(c.getDeclaredMethod("value").getReturnType() == Class.class,
              "value() returns Class<? extends Annotation>");
        check(c.getDeclaredMethod("value").getDefaultValue() == null, "value() has no default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        // meta-annotation readable: value() points at the container annotation type
        Repeatable r = Schedule.class.getAnnotation(Repeatable.class);
        check(r != null, "Schedule has @Repeatable");
        check(r.value() == Schedules.class, "value() is the container type");

        // repeated usage: getAnnotationsByType sees both instances in source order
        Schedule[] all = Job.class.getAnnotationsByType(Schedule.class);
        check(all.length == 2, "two repeated instances");
        check(all[0].cron().equals("0 0 * * *") && all[0].order() == 1, "first instance values");
        check(all[1].cron().equals("0 30 6 * *") && all[1].order() == 2, "second instance values");

        // single direct annotation is not exposed via getAnnotation (container is)
        check(Job.class.getAnnotation(Schedule.class) == null,
              "getAnnotation does not expose repeated instances");
        check(Job.class.getAnnotation(Schedules.class) != null,
              "getAnnotation exposes the container");

        // container value round-trip
        Schedules container = Job.class.getAnnotation(Schedules.class);
        check(container.value().length == 2, "container holds both");
        check(container.value()[0].equals(all[0]) && container.value()[1].equals(all[1]),
              "container members equal to getAnnotationsByType results");

        // single usage: no container in getAnnotations
        check(Single.class.getAnnotationsByType(Schedule.class).length == 1, "single instance");
        check(Single.class.getAnnotation(Schedule.class) != null,
              "single usage exposed directly");
        boolean hasContainer = false;
        for (Annotation a : Single.class.getAnnotations()) {
            if (a.annotationType() == Schedules.class) hasContainer = true;
        }
        check(!hasContainer, "no container annotation for single usage");

        // declaring-side constraint: container must have T[] value()
        try {
            java.lang.reflect.Method v = Schedules.class.getMethod("value");
            check(v.getReturnType().getComponentType() == Schedule.class,
                  "container value() is Schedule[]");
        } catch (NoSuchMethodException e) {
            check(false, "container missing value(): " + e);
        }

        // equals/hashCode across the two access paths
        Schedule firstAgain = Job.class.getAnnotationsByType(Schedule.class)[0];
        check(all[0].equals(firstAgain), "equal instances across calls");
        check(all[0].hashCode() == firstAgain.hashCode(), "hashCode consistent");
        check(!all[0].equals(all[1]), "distinct instances not equal");

        // a container that is not itself @Repeatable cannot be repeated directly
        boolean repeatedContainer = Schedules.class.getAnnotation(Repeatable.class) != null;
        check(!repeatedContainer, "container not itself repeatable");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
