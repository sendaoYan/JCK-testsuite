# java.lang.reflect.AnnotatedElement — 测试设计（JDK-8）

## 1. API 功能
`AnnotatedElement` 是可承载注解的反射对象统一接口（`Class`/`Package`/`Method`/`Constructor`/`Field`/`Parameter`/`ParameterizedType` 等实现）：
- `isAnnotationPresent`（含 `@Inherited` 继承可见）、`getAnnotation`（继承单值）。
- `getAnnotations`（继承视图）vs `getDeclaredAnnotations`（仅直接声明）——继承语义核心区分。
- `getAnnotationsByType`/`getDeclaredAnnotationsByType`：`@Repeatable` 容器展平为成员注解数组；重复时 `getAnnotation` 返回 null（契约与 `isAnnotationPresent` 一致）。
- `getDeclaredAnnotation`：仅直接存在单值。
- null `Class` 参数统一 NPE（实测 jdk8u372-b07：五个查询方法全抛）。

## 2. Java 用户如何使用
以接口引用任意程序元素 → 查询某注解存在与否与取值 → 驱动框架（注入点识别、路由映射、配置绑定、测试发现）。可重复注解必须走 `getAnnotationsByType` 才能拿到多个；`@Inherited` 只对类有效且只沿超类链。

## 3. 参考来源
- Oracle javadoc（Java 8 `java.lang.reflect.AnnotatedElement`）：继承/直接存在/关联语义定义、`@Repeatable` 展平规则。
- OpenJDK 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/AnnotatedElement.java`（默认方法实现：`getDeclaredAnnotationsByType` 先查容器）；`sun/reflect/annotation/AnnotationSupport.java`（展平算法）。
- OpenJDK 回归：`~/git/jdk/test/jdk/java/lang/reflect/AnnotatedElement/TestAnnotatedElementDefaults.java`、`~/git/jdk/test/jdk/java/lang/annotation/Repeatable/`、`Inherited/`。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/annotation/`（Repeatable/Inherited 边界矩阵）。

## 4. 测试用例设计 + 易错规范细节
注解 fixture 全部自造：`@Tag`/`@Tags`（`@Repeatable` 容器对）、`@InhMark`（`@Inherited`）、`@PlainMark`、`@Ord1..3`（顺序探针）、参数注解 `@Tag("p1")`。

| test 方法 | 场景 | 关键断言 |
|---|---|---|
| testAnnotatedElement | 白盒保留 | `@Deprecated` 类七查询 + Plain 三负例 |
| testRepeatableAnnotations | 重复 3 连 | byType 展平 3 且保声明序；`getAnnotation(Tag)`==null；容器直取 `getAnnotation(Tags)` 非 null 持 3；直接视图长度 2（容器+其它）且容器保持声明位；单次出现不包容器（`getAnnotation(Tags)` null） |
| testInheritedSemantics | 继承链 | 子类 get/is/byType 全可见、getDeclared 三路全不可见；孙类仍可见；父类 declared 非 null；子类自身注解与继承注解并存（getAnnotations 2 / getDeclared 1）；无关类不受影响 |
| testDeclarationOrder | 声明顺序 | `getDeclaredAnnotations` 按源码声明序 `[Ord1,Ord2,Ord3]`（8 实现稳定） |
| testMemberCarriers | 跨成员一致 | Field（值 f1）/Method（m1）/Constructor（全空）/Parameter（p1）统一契约 |
| testNullArguments | 负例 | Class/Field/Method/Constructor/Parameter 载体上 5 个查询方法 null 参数全 NPE |
| testAbsentPaths | 缺失路径 | 全部查询返回 0/null/false 不抛 |

易错规范细节：
1. `isAnnotationPresent(X)` 等价 `getAnnotation(X) != null`——重复注解时**两者都为 false**（直接存在的是容器），必须用 byType 才见成员。
2. `@Inherited` 注解对接口/方法/字段无效，只沿超类链；`getDeclaredAnnotations` 永不含继承注解。
3. 单次出现的可重复注解**不**生成容器（`getAnnotation(容器)` 为 null）。
4. 直接视图中容器占据成员注解首个声明位置，数组顺序=声明顺序（容器视作一个元素）。
5. jtreg 头注释绝不能出现元注解字样（jtreg 解析坑），元注解只写在代码里。

产出：`AnnotatedElementTest.java`（86 断言通过，jtreg 全绿）。
