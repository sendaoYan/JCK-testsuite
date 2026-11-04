# java.lang.reflect.Parameter — 测试设计（JDK-27）

## 1. API 功能
`Parameter` 表示方法/构造器的单个参数（经 `Executable.getParameters()` 获得）：
- 身份：`getName/isNamePresent/getDeclaringExecutable/getModifiers/accessFlags`。
- 类型：`getType`（擦除）、`getParameterizedType`（泛型）、`getAnnotatedType`。
- 形态：`isVarArgs/isImplicit/isSynthetic`。
- 注解：实现 `AnnotatedElement` 全套（`getAnnotation/getAnnotations/getDeclaredAnnotation/...`）。
- 相等：`equals/hashCode/toString`。

## 2. Java 用户如何使用
`method.getParameters()` 按位置遍历 → `getType/getParameterizedType` 做参数匹配/序列化 → `isVarArgs` 识别尾参 → `getName`（未用 `-parameters` 编译时为合成名 `argN` 且 `isNamePresent()==false`）仅做展示。框架（Spring MVC 参数解析、Jackson creator 检测）依赖类型+注解组合。

## 3. 开源代码实际用法调研
- JDK 自测 `~/git/jdk25u-dev/test/jdk/java/lang/reflect/Parameter/`（`ParameterWithName.java`/`NoName.java`）：`isNamePresent` 与 `argN` 命名、`-parameters` 行为对照。
- JDK 自测 `AccessFlag/RequiredMethodParameterFlagTest.java`：`Parameter.accessFlags`/`getModifiers` 对隐式参数的形态。
- Spring `MethodParameter`（spring-core）以 `Parameter` 为底层数据源做参数名/泛型解析（同构场景 1/4）。
- Guice/Hibernate Validator：参数注解驱动的校验链路（场景 3）。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`ParameterTest.testBlackBox`，固定类 `Sample/Generics/Outer.Inner`）：
1. 位置化类型/擦除泛型/名字：`argN` 与 `isNamePresent` 双分支（编译器开关无关）、toString 含类型。
2. varargs 尾参 `isVarArgs` 且类型为数组。
3. 参数注解：`@Deprecated` 参数与非注解参数 6 项对照查询 + `getAnnotatedType`。
4. 泛型参数：`<T>` 擦除为 Object 但 `getParameterizedType` 为 `TypeVariable("T")`；`List<String>` 参数为 `ParameterizedType` 精确 typeName。
5. 内部类构造器隐式首参：`isImplicit` true、非 synthetic、类型为外部类、非 public 修饰。
6. `equals/hashCode` 值语义与外类型不等、plain 参数零修饰符/空 accessFlags/`isImplicit/isSynthetic` false。
白盒保留原有结构断言。产出：`ParameterTest.java`（42 断言通过）。

## 深化批次（2026-11-05）

新增 4 个 test 方法（44 → 55 断言）：

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 默认编译 argN 锁定：isNamePresent()==false、arg0/arg1、toString=="int arg0" | testNameAbsenceContract |
| P0 | varargs 位置敏感性：固定参数 false、末位 true、数组类型（getType 与 getParameterizedType） | testVarargsPosition |
| P1 | 构造器参数视图：合成名/back-reference/toString | testCtorParameterViews |
| P0 | 内部类构造器 implicit 首参位锁定：modifiers==0x8010（FINAL|MANDATED）、accessFlags=={FINAL, MANDATED} | testInnerCtorParamFlagsLocked |

### 锁定的版本差异（本批实测）
- 内部类构造器首参（外围实例）：JDK-27 isImplicit()==true、modifiers 0x8010；JDK-8 无 -parameters 时报 false/0。
- `Parameter.accessFlags()`：JDK-27 有（JDK 20+）、JDK-8 无。
