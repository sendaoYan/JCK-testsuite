# java.lang.Class 测试设计案（JDK-8）

> 覆盖目标：**完整契约**，不是「getName 读一下」的冒烟。重点补：原始类型/引用类型/数组的名称族
> （getName/getSimpleName/getCanonicalName/getTypeName）、superclass/interfaces 层级（Object 超类为
> null、接口超类为 null、枚举超类 Enum）、primitive/array/enum/annotation 分类、数组 componentType、
> forName（含 init flag 与 ClassNotFoundException、数组描述符 [I）、类加载器（bootstrap null）、
> isInstance/isAssignableFrom/cast/asSubclass（含 CCE）、modifiers、declared 成员内省、匿名/局部/成员类
> 分类（isAnonymousClass/isLocalClass/isMemberClass/isSynthetic）。断言确定，headless 安全。

## 1. API 提供了哪些功能

- **类**：`java.lang.Class<T>`（final，implements `Serializable`/`Type`/`AnnotatedElement` 等）。
- **名称**：`getName`/`getSimpleName`/`getCanonicalName`/`getTypeName`（JDK-8 有 getTypeName，Java 5+）。
- **层级**：`getSuperclass`/`getInterfaces`/`isInterface`/`isEnum`/`isAnnotation`/`isPrimitive`/`isArray`。
- **类型关系**：`isInstance`/`isAssignableFrom`/`cast`/`asSubclass`。
- **加载**：`forName`（3 个重载）、`getClassLoader`。
- **成员/修饰符**：`getModifiers`/`getDeclaredMethods`/`getDeclaredConstructors`/`getComponentType`。
- **类形态**：`isAnonymousClass`/`isLocalClass`/`isMemberClass`/`isSynthetic`。

## 2. Java 用户如何使用

- `String.class.getName()` 得到 `java.lang.String`；`int[].class.getName()` 得到 `[I`。
- `Class.forName(name)` 反射加载；`Number.class.cast(obj)` 强转；`clazz.asSubclass(If.class)` 窄化 Class 泛型。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/lang/Class.html`）：名称族、层级、forName、cast/asSubclass 契约。
- **OpenJDK 8u-dev 源码** `~/git/jdk8u-dev/jdk/src/share/classes/java/lang/Class.java`。
- **实测 JDK-8 运行时**（Temurin 8u372）：bootstrap 类 `getClassLoader()==null`、数组描述符格式。
- **OpenJDK 回归** `~/git/jdk8u-dev/jdk/test/java/lang/Class`。

## 4. 测试用例设计（映射到 ClassTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | 原始类型：isPrimitive/名称/superclass null/非数组/非接口/非枚举/非注解 | testPrimitive |
| P0 | 名称族：String 与 String[]/int[] 的四种名称 | testNames |
| P0 | 层级：Object 超类 null、String/Integer/Number/Member 超类、注解超类 null | testHierarchy |
| P0 | 接口：Runnable/Iface、getInterfaces、superclass null | testInterfaces |
| P0 | 数组：isArray/getComponentType/superclass Object/非 primitive | testArrays |
| P0 | 枚举/注解：isEnum/超类 Enum/isAnnotation | testEnumAnnotation |
| P0 | isInstance/isAssignableFrom/cast（含 CCE） | testIsInstanceIsAssignableCast |
| P0 | asSubclass（含 CCE） | testAsSubclass |
| P0 | forName：已知类/带 init/数组描述符/缺失 CNFE | testForName |
| P0 | 类加载器：bootstrap null、用户类加载器非 null | testClassLoader |
| P0 | 修饰符：public/final/interface/static | testModifiers |
| P0 | declared 成员内省 | testDeclaredMembers |
| P1 | 类形态：匿名/局部/成员类、isSynthetic | testClassKinds |

## 关键规范细节（易错，已锁入断言）

- **`int.class.getName()=="int"`、`int[].class.getName()=="[I"`、`String[].class.getName()=="[Ljava.lang.String;"`**。
- **`Object.class.getSuperclass()==null`（无超类）；接口和注解的 superclass 也是 null**；枚举超类为 `Enum`。
- **`getTypeName()` 是 Java 5+ 方法**（JDK-8 有），与 `getCanonicalName` 对数组都返回 `int[]`。
- **bootstrap 加载的类（String/int）`getClassLoader()==null`**；`Class.forName("[I")` 返回 int[]。
- **`cast`/`asSubclass` 类型不匹配抛 `ClassCastException`**；`isAssignableFrom` 自反、`Object` 可赋自数组。
- **`getDeclaredConstructors().length==1` 用于默认构造器的嵌套类**。

## 注意事项

- headless 安全，不加 `@key`；结果确定；不加载外部类（forName 只用 java.lang 与数组描述符）。
