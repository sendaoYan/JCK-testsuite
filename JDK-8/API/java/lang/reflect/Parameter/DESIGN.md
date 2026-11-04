# java.lang.reflect.Parameter — 测试设计（JDK-8）

## 1. API 功能

`Parameter`（JDK-8 引入）描述 `Executable` 的单个形参：
- 身份：`getDeclaringExecutable/getModifiers/isImplicit/isSynthetic/isVarArgs`。
- 名称：`getName/isNamePresent`——`isNamePresent` 仅当 classfile 携带 MethodParameters
  属性（javac `-parameters`）时为 true，否则 `getName()` 返回合成名 `argN`。
- 类型：`getType`（擦除）/`getParameterizedType`（泛型保留）/`getAnnotatedType`。
- 实现 `AnnotatedElement`：`getAnnotation/getAnnotations/getDeclaredAnnotation(s)/
  getAnnotationsByType/getDeclaredAnnotationsByType/isAnnotationPresent`。
- JDK-8 无 `accessFlags()`（JDK 20 才加入）。

## 2. Java 用户如何使用

参数名驱动框架体验：Spring `@RequestParam` 名字解析、MyBatis `#{paramName}`、
Hibernate Validator 参数校验消息——这些都要求编译期加 `-parameters`；未加时框架
只能得到 argN 回退名。类型视图用于泛型校验器（如 `List<String>` 元素校验）。

## 3. 参考来源（只列路径）

- Oracle Java SE 8 javadoc：`java/lang/reflect/Parameter.html`。
- OpenJDK 8 源码：`~/git/jdk8u-dev/jdk/src/share/classes/java/lang/reflect/Parameter.java`。
- OpenJDK 回归测试：`~/git/jdk/test/jdk/java/lang/reflect/Parameter/`（含 `-parameters` 编译矩阵）。
- JCK runtime-8b：`~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_lang/reflect/Parameter/`。

## 4. 测试用例设计（ParameterTest.java，12 → 47 断言）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 保留原结构断言（类型/声明者/varargs） | testParameter |
| P0 | 默认编译（无 -parameters）：isNamePresent()==false、getName()=="arg0/arg1"、toString=="int arg0" | testNameAbsenceContract |
| P0 | varargs 位置敏感性：固定参数 false、末位 true、类型是数组、plain 标志 | testVarargsPosition |
| P0 | 泛型参数视图：TypeVariable "T"、`List<String>` 的 ParameterizedType 与 typeName 全等、AnnotatedType 保 TV | testGenericParameterViews |
| P1 | equals/hashCode 跨查询稳定、不同位置不等、不等于 null/异类 | testEqualsHashCodeContract |
| P1 | 参数注解（getAnnotation/getAnnotations/isAnnotationPresent/getDeclaredAnnotation） | testAnnotationOnParameter |
| P0 | 内部类构造器参数：首参=外围类实例；**JDK-8 无 -parameters 时 isImplicit()==false** | testInnerCtorParameters |

## 易错规范细节（已锁入断言）

- jtreg 默认编译不带 `-parameters`，故 `argN` 分支是确定性契约（jtreg 环境锁定）。
- `getAnnotatedType().getType()` 对泛型参数返回 **TypeVariable**（非擦除后的 Class），两侧一致。
- 内部类构造器首参（外围实例）：JDK-8 无法从签名推导 implicit（报 false、modifiers 0）；
  JDK-9+ 报 true 且 modifiers=0x8010（FINAL|MANDATED）——已按侧锁定。
- `varMixed(int fixed, int... rest)`：isVarArgs 只在**末位**参数为 true。
