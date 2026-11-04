# java.lang.reflect.AnnotatedElement — 测试设计（JDK-27）

## 1. API 功能
`AnnotatedElement` 是可承载注解的反射对象统一接口（Class/Package/Method/Constructor/Field/Parameter/RecordComponent/TypeVariable 实现）：
- `isAnnotationPresent/getAnnotation`（直接+继承）。
- `getAnnotations/getDeclaredAnnotations`（含 `@Inherited` 传播 vs 仅直接）。
- `getAnnotationsByType/getDeclaredAnnotationsByType`：可重复注解展平；`getDeclaredAnnotation`：仅直接存在单值。
- 默认方法全部由 `AnnotatedElement` 提供（抽象方法仅 `getAnnotation/getAnnotations/getDeclaredAnnotations`）。

## 2. Java 用户如何使用
以接口引用任意程序元素 → 查询某注解存在与否与取值 → 驱动框架行为（注入点识别、路由映射、配置绑定）。可重复注解需用 `getAnnotationsByType` 才能看到多个；`@Inherited` 只对类有效。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/AnnotatedElement/TestAnnotatedElementDefaults.java`：默认方法与直接/继承语义。
- JDK 自测 `test/jdk/java/lang/annotation/`（`Repeatable/`、`Inherited/` 目录）：可重复与继承行为（同构场景 1/2）。
- Spring `AnnotatedElementUtils.findMergedAnnotation`：以 `getAnnotations*` 为底座做元注解合并（场景 3）。
- JUnit 5 `AnnotatedElementFinder`：`isAnnotationPresent` 驱动测试发现（场景 3）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`AnnotatedElementTest.testBlackBox`，自定义 `@Tag/@Tags/@Repeatable/@Inherited`）：
1. 可重复注解：`getAnnotationsByType` 展平为 2 且保声明序、重复时 `getAnnotation(Tag)` 为 null、直接视图是容器 + 长度。
2. `@Inherited`：子类可见（get/isAnnotationPresent/getAnnotationsByType）但 `getDeclaredAnnotation` 为 null；父类声明非 null；无关类不受影响。
3. 契约跨成员一致：字段（取值 f1）/方法/构造器（无注解）统一查询。
4. 缺失类型零返回 + `isAnnotationPresent(null)` NPE。
5. 容器即直接声明注解且持双值。
6. record 组件注解按 `@Target` 传播到访问器（组件自身非 METHOD 目标时为 null）。
白盒保留原简单注解断言（独立 `@Deprecated` 类）。

Part 3 补充契约维度（`testDeclarationOrder/testRepeatableDepth/testInheritedPlusDeclared/testNullContract/testParameterCarrier`，自造 `@Ord1..3`/`@PTag(PARAMETER)`）：
1. 声明顺序：`getDeclaredAnnotations` 按源码序 `[Ord1,Ord2,Ord3]` 精确数组。
2. 重复注解对照：单次出现 `getAnnotation(Tag)` 非 null 且无容器；重复时展平/声明 byType 均双元素。
3. 继承+自身并存：子类自身 `@Tag` + 父类 `@Inh` → getAnnotations 2 / getDeclared 1 / 继承 byType 1 declared-byType 0。
4. null 契约（27 全 NPE）：五个查询方法在 Class/Field/Method/Parameter 载体上 null 参数全抛 NPE。
5. `Parameter` 载体（注解需 `PARAMETER` 目标，用自造 `@PTag`）：present/取值/长度/declared/byType。

产出：`AnnotatedElementTest.java`（68 断言通过，jtreg 全绿）。
