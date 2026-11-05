/*
 * @test
 * @summary java.lang.annotation.Repeatable conformance: white-box structure + black-box behavior,
 *          value() container pointer, repeated usage, container aggregation, and meta-annotation
 *          contract.
 * @run main RepeatableTest
 */

import java.lang.annotation.Annotation;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
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

    // three repetitions: any number of occurrences aggregates into one container
    @Schedule(cron = "a", order = 1)
    @Schedule(cron = "b", order = 2)
    @Schedule(cron = "c", order = 3)
    static class Triple {
    }

    // ---- Part 1: white-box (structural) ----
    static void testWhiteBox() throws Exception {
        Class<?> c = Class.forName("java.lang.annotation.Repeatable");
        check(c.isAnnotation(), "Repeatable is an annotation type");
        check(c.isAnnotationPresent(Retention.class)
              && c.getAnnotation(Retention.class).value() == RetentionPolicy.RUNTIME,
              "@Retention(RUNTIME)");
        check(c.isAnnotationPresent(Documented.class), "@Documented on Repeatable");
        Target t = c.getAnnotation(Target.class);
        check(t != null && t.value().length == 1 && t.value()[0] == ElementType.ANNOTATION_TYPE,
              "@Target(ANNOTATION_TYPE)");
        check(c.getDeclaredMethods().length == 1, "single member");
        check(c.getDeclaredMethod("value").getReturnType() == Class.class,
              "value() returns Class<? extends Annotation>");
        check(c.getDeclaredMethod("value").getDefaultValue() == null, "value() has no default");
    }

    // ---- Part 2: black-box (behavioral) ----
    static void testBlackBox() {
        Repeatable r = Schedule.class.getAnnotation(Repeatable.class);
        check(r != null, "Schedule has @Repeatable");
        check(r.value() == Schedules.class, "value() is the container type");

        Schedule[] all = Job.class.getAnnotationsByType(Schedule.class);
        check(all.length == 2, "two repeated instances");
        check(all[0].cron().equals("0 0 * * *") && all[0].order() == 1, "first instance values");
        check(all[1].cron().equals("0 30 6 * *") && all[1].order() == 2, "second instance values");

        check(Job.class.getAnnotation(Schedule.class) == null,
              "getAnnotation does not expose repeated instances");
        check(Job.class.getAnnotation(Schedules.class) != null,
              "getAnnotation exposes the container");

        Schedules container = Job.class.getAnnotation(Schedules.class);
        check(container.value().length == 2, "container holds both");
        check(container.value()[0].equals(all[0]) && container.value()[1].equals(all[1]),
              "container members equal to getAnnotationsByType results");

        check(Single.class.getAnnotationsByType(Schedule.class).length == 1, "single instance");
        check(Single.class.getAnnotation(Schedule.class) != null,
              "single usage exposed directly");
        boolean hasContainer = false;
        for (Annotation a : Single.class.getAnnotations()) {
            if (a.annotationType() == Schedules.class) hasContainer = true;
        }
        check(!hasContainer, "no container annotation for single usage");

        try {
            java.lang.reflect.Method v = Schedules.class.getMethod("value");
            check(v.getReturnType().getComponentType() == Schedule.class,
                  "container value() is Schedule[]");
        } catch (NoSuchMethodException e) {
            check(false, "container missing value(): " + e);
        }

        Schedule firstAgain = Job.class.getAnnotationsByType(Schedule.class)[0];
        check(all[0].equals(firstAgain), "equal instances across calls");
        check(all[0].hashCode() == firstAgain.hashCode(), "hashCode consistent");
        check(!all[0].equals(all[1]), "distinct instances not equal");

        boolean repeatedContainer = Schedules.class.getAnnotation(Repeatable.class) != null;
        check(!repeatedContainer, "container not itself repeatable");
    }

    // ---- Part 3: deepened repetition semantics ----
    static void testDeepenedRepetition() throws Exception {
        // third and further repetitions: all flattened by getAnnotationsByType in order
        Schedule[] triple = Triple.class.getAnnotationsByType(Schedule.class);
        check(triple.length == 3, "three repeated instances");
        check("a".equals(triple[0].cron()) && triple[0].order() == 1, "triple first values");
        check("b".equals(triple[1].cron()) && triple[1].order() == 2, "triple second values");
        check("c".equals(triple[2].cron()) && triple[2].order() == 3, "triple third values");

        // the declared view holds ONLY the container, never the individual instances
        Annotation[] declared = Triple.class.getDeclaredAnnotations();
        check(declared.length == 1, "one declared annotation (the container): " + declared.length);
        check(declared[0].annotationType() == Schedules.class, "declared annotation is container");
        check(Triple.class.getDeclaredAnnotation(Schedule.class) == null,
              "getDeclaredAnnotation does not synthesize repeated instances");
        check(Triple.class.getDeclaredAnnotation(Schedules.class) != null,
              "getDeclaredAnnotation exposes the container");
        check(Triple.class.getAnnotationsByType(Schedules.class).length == 1,
              "container type queried by-type returns the single container");

        // the container's value() is also defensively copied on every call
        Schedules container = Triple.class.getAnnotation(Schedules.class);
        Schedule[] vals = container.value();
        vals[0] = null;
        check(container.value()[0] != null && "a".equals(container.value()[0].cron()),
              "container value() defensively copied");

        // the Repeatable meta-annotation itself is not repeatable and reads back exactly
        check(Repeatable.class.getAnnotation(Repeatable.class) == null,
              "Repeatable is not itself @Repeatable");
        java.lang.reflect.Method v = Repeatable.class.getMethod("value");
        check(v.toString().equals(
                "public abstract java.lang.Class java.lang.annotation.Repeatable.value()"),
              "Repeatable.value() exact signature: " + v);

        // container equality/hashCode are value-based like any annotation
        Schedules again = Triple.class.getAnnotation(Schedules.class);
        check(container.equals(again), "container equal across fetches");
        check(container.hashCode() == again.hashCode(), "container hashCode stable");
        check(!container.equals(Job.class.getAnnotation(Schedules.class)),
              "containers with different contents not equal");
    }

    public static void main(String[] args) throws Exception {
        testWhiteBox();
        testBlackBox();
        testDeepenedRepetition();
        System.out.println("PASS (" + passed + " assertions)");
    }
}
