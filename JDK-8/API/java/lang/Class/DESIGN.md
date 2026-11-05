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

## 契约深化（2026-11）

### 1. API 功能（本轮新增维度）
- 名称全矩阵：9 个原始类型（含 void）的 getName/getSimpleName/getCanonicalName/getTypeName/toString 与包装类 TYPE 同一性；数组描述符全表（[Z[B[C[S[I[J[F[D、嵌套数组 [[I、引用数组 [Ljava.lang.String;）、数组 simple/canonical/typeName 三件套。
- 命名族实测锁定：嵌套类 getName 为 "Outer$Inner"、匿名类 canonical null / simple ""、局部类 canonical null（simple 保留）、lambda 类名含 "Lambda" 且 isSynthetic。
- cast/isInstance/asSubclass 契约：cast 同对象恒等、cast(null)==null、原始类型 cast 非 null 必抛 CCE（消息 "Cannot cast java.lang.Integer to int" 精确锁定）、isInstance(null) 恒 false（含 Object.class）、原始类型 isInstance 恒 false。
- newInstance（弃用但可用）：public 无参 OK、System 私有构造 IllegalAccessException、抽象类/接口/无无参构造 InstantiationException、**JDK-8 编译产物无 nestmate 属性 → 嵌套私有构造 IllegalAccessException**（与 27 差异）；getConstructor(int,int) 精确签名（Integer 装箱不匹配 → NoSuchMethodException，消息含 "<init>(java.lang.Integer, java.lang.Integer)"）。
- 字段/方法黑盒：getField/getFields（含继承 public 字段）、getDeclaredMethod 精确签名区分 foo(int)/foo(Integer)、NoSuchFieldException 消息==字段名、NoSuchMethodException 消息格式 "Class$n.method(params)" 精确锁定、接口 getMethods 不含 Object 方法（实测）。
- getEnumConstants：与 values() 逐一同一、每次调用返回防御拷贝（改数组不影响下次）。
- forName 初始化闭环：计数器放独立类 InitCounter（避免读 count 本身触发初始化）——forName(false) 计 0、首次 forName(true) 计 1、重复调用恒 1；数组描述符（[I/[[I/[Ljava.lang.String;/嵌套 [LClassTest$Color;）全部 == 类字面量；负例消息精确："java/lang/String[]"（源码形式数组名，斜杠分隔）、"int"（原始类型名不可 forName）、"java.lang.DoesNotExist"。
- 资源/包：getResourceAsStream 绝对/相对路径、字节流前 4 字节 CAFEBABE、原始类型资源 null、default 包 getPackage()==null（JDK-8）、desiredAssertionStatus false、getProtectionDomain 非 null。

### 2. 使用方式
同上（静态嵌套 fixture + check 计数）。

### 3. 参考来源
Oracle javadoc（Class.getName 数组编码、forName initialize 语义、cast CCE 消息、getEnumConstants 拷贝语义、TYPE 常量）；JLS 13.1（二进制名）；双侧 JDK 实测（异常消息、toGenericString 数组形式）。

### 4. 测试用例设计 + 易错规范细节
- **易错 1**：forName 初始化计数若把 counter 放目标类自身，读取 counter 就会触发初始化——必须拆到独立计数类。
- **易错 2**：`int[].class.toGenericString()` 在 JDK-8 是 "public abstract final class [I"（descriptor 形式），JDK-27 是 "int[]"——版本差异两侧分别锁定。
- **易错 3**：接口 getMethods() 不含 Object 的公有方法（toString/equals 找不到）；类则包含。
- **易错 4**：嵌套类 toGenericString 带 "static" 前缀（"static class ClassTest$Member"）。
- **易错 5**：构造器 NoSuchMethodException 消息参数分隔符为 ", "（带空格）。
- 覆盖：testPrimitiveNameMatrix/testArrayNameMatrix/testNestedNamesDeep/testToStringFamily/testCastContract/testAsSubclassContract/testNewInstanceContract/testFieldLookup/testMethodLookup/testInterfacesContract/testEnumConstantsContract/testForNameInitialization/testResourcePackageMisc/testSuperclassGeneric/testSyntheticAnnotationFlags。
