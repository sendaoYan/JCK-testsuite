# java.lang.annotation.Annotation — 设计方案

## 1. API 功能
所有注解类型隐式实现的根接口：
- 契约方法：`equals(Object)`（成员值相等）、`hashCode()`（规范公式）、`toString()`（可读形式）
- 元信息：`annotationType()`（返回注解的 `Class` 对象）

## 2. Java 用户如何使用
自定义 `@interface` 后由编译器生成实现（运行期为动态代理）：
```java
@interface Valued { String name(); int count() default 1; }
@Valued(name = "x", count = 7) class A {}
Valued v = A.class.getAnnotation(Valued.class);
v.name(); v.count(); v.annotationType(); v.toString(); // @pkg.Valued(count=7, name="x")
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/sun/reflect/annotation/AnnotationInvocationHandler.java`：`equalsImpl` 对非代理对象逐成员 invoke 比较；`toStringImpl` 用 canonical name + 成员名排序；`hashCodeImpl` 按规范公式。
- JDK `test/jdk/java/lang/annotation/EqualityTest.java`、`AnnotationToStringTest.java`：验证 equals/hashCode/toString 契约（含字符串带引号、单 value 成员省略名）。
- Spring `@Component`/`@Autowired` 等元注解体系：`AnnotationUtils.synthesizeAnnotation` 依赖 `annotationType()` 遍历成员。

## 4. 测试用例设计 + 自动化实现
白盒：接口、恰 4 个抽象方法（equals/hashCode/toString/annotationType）及签名。
黑盒：
- marker 注解：annotationType、equals/hashCode 一致、与异类型/null/String 不等；
- 带成员注解：成员读取、toString 精确格式（canonical 名、成员按名排序、字符串加引号）；
- 实例是动态代理（Proxy API 可识别）；
- equals 接受手工实现的注解实例；hashCode 与规范公式 `(127*name.hashCode())^value.hashCode()` 求和一致；
- `getAnnotations()` 家族返回注解数组。
对应 `AnnotationTest.java` 的 `testBlackBox()`。
