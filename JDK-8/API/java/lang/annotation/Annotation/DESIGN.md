# java.lang.annotation.Annotation — 设计方案（JDK 8）

## 1. API 功能
所有注解类型隐式实现的根接口：
- `equals(Object)`：同注解类型且同名成员值逐一相等（数组按 Arrays.equals）
- `hashCode()`：规范公式 `Σ (127 * 成员名.hashCode()) ^ 成员值.hashCode()`
- `toString()`：`@全限定名(名=值,…)`，**JDK 8 旧格式**（见“易错细节”）
- `annotationType()`：返回注解的 `Class`

## 2. Java 用户如何使用
自定义 `@interface` 后由编译器生成运行期动态代理实现：
```java
@interface Rich { int i(); String s(); String[] sa(); Nested n(); }
@Rich(i=1, s="x", sa={"a"}, n=@Nested(tag="t")) class A {}
Rich r = A.class.getAnnotation(Rich.class);
r.i(); r.s(); r.sa(); r.n(); r.annotationType(); r.toString();
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationInvocationHandler`：equalsImpl/hashCodeImpl/toStringImpl 的实现来源；JDK 8 的 toStringImpl 输出旧格式。
- JDK `test/jdk/java/lang/annotation/EqualityTest.java`、`AnnotationToStringTest.java`。
- Spring `AnnotationUtils.synthesizeAnnotation` 依赖 `annotationType()` 遍历成员。

## 4. 测试用例设计 + 自动化实现
- 结构：接口 + 恰 4 个抽象方法及签名。
- marker：annotationType/equals/hashCode/与 null/String/异类型不等。
- 多成员 Rich/Twin：同名同型但不同注解类型 → 不等；同型同值 → 相等；任一成员异 → 不等。
- 成员种类全覆盖：int/boolean/String/Class/枚举/数组/默认值/嵌套注解；数组成员防御性拷贝。
- hashCode 规范公式（对简单 Valued 精确求和）。
- toString：JDK 8 旧格式断言（二进制名 `$`、字符串不加引号、Class 用 `class X`、数组用 `[..]`）。
- 动态代理（Proxy.isProxyClass）+ getAnnotations 家族。

## 易错规范细节（JDK 8）
- **toString 与 JDK 27 不同**：JDK 8 用 `getName()`（二进制名，嵌套类为 `A$Rich`）、字符串不加引号（`s=x`）、Class 显示为 `class java.lang.String`、数组显示为 `[a, b]`；JDK 27 用 canonical 名（`A.Rich`）、字符串加引号、Class 用 `java.lang.String.class`、数组用 `{a, b}`。
- 成员顺序实现相关，规范不保证；断言用 contains/前缀，不用整体精确顺序。
- 数组成员由代理按需克隆返回，修改副本不影响注解数据。

## 契约深化（2026-11）
### API 功能
- 接口白盒：`Annotation` 恰 4 个声明方法（equals/hashCode/toString/annotationType），全抽象、无默认方法、无字段（javap 双侧一致）。
- 成员代理全家族扩展：`@Prim`（byte/short/char/long/float/double/boolean 七种基本类型成员全给值精确回读）、`@ArrAll`（String[]+8 种基本类型数组默认值回读 + 五类数组防御性拷贝逐个实证）、`@WithClass`（Class 成员身份语义）。
- hashCode 规范公式扩展锁定：基本类型按包装器 hashCode、数组按 `Arrays.hashCode`、Class 成员按 Class 对象自身 hashCode（同 JVM 内确定）；marker 注解 hashCode==0。
- 注解相等为纯值相等：两个不同元素携带同值 → equals true、hashCode 相等。

### 使用方式
`getXxx()` 精确值断言 + 改返回数组后再取原值 + 公式求和比对 hashCode。

### 参考来源
javap 双侧（java.lang.annotation.Annotation）；sun.reflect.annotation.AnnotationInvocationHandler 行为（仅思路）；Oracle javadoc Annotation.hashCode 公式。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testInterfaceWhiteBox`：getDeclaredMethods()==4 全抽象、equals 恰 1 参、无字段。
- `testDeepenedMemberContract`：Prim/ArrAll/WithClass 全族；hashCode 公式 43 项逐项求和锁死。
- 易错：Class 成员 hashCode 是**身份 hash**（同一次 JVM 运行内确定，跨运行不确定），不能与 getName().hashCode() 混淆；`==` 优先级高于 `^`，公式断言必须整体加括号。
