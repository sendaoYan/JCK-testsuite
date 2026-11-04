# java.lang.annotation.Annotation — 设计方案（JDK 27）

## 1. API 功能
所有注解类型隐式实现的根接口：
- 契约方法：`equals(Object)`（同类型且同名成员值相等）、`hashCode()`（规范公式）、`toString()`（可读形式）
- 元信息：`annotationType()`（返回注解的 `Class`）

## 2. Java 用户如何使用
```java
@interface Valued { String name(); int count() default 1; }
@Valued(name = "x", count = 7) class A {}
Valued v = A.class.getAnnotation(Valued.class);
v.name(); v.count(); v.annotationType(); v.toString(); // @pkg.Valued(count=7, name="x")
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationInvocationHandler`：equalsImpl/hashCodeImpl/toStringImpl 实现来源。
- JDK `test/jdk/java/lang/annotation/EqualityTest.java`、`AnnotationToStringTest.java`。
- Spring `AnnotationUtils.synthesizeAnnotation` 依赖 `annotationType()`。

## 4. 测试用例设计 + 自动化实现
白盒：接口 + 恰 4 个抽象方法及签名。
黑盒：
- marker：annotationType/equals/hashCode/与 null/String/异类型不等/自反。
- 多成员 Rich/Twin：同名同型但不同注解类型 → 不等；同型同值 → 相等；任一成员异 → 不等。
- 成员种类全覆盖：int/boolean/String/Class/枚举/数组/默认值/嵌套注解；数组成员防御性拷贝。
- hashCode 规范公式（简单 Valued 精确求和）+ 相等实例 hashCode 一致。
- toString：JDK 27 canonical 格式断言（`.` 名、字符串加引号、Class 用 `.class`、数组用 `{..}`）。
- 动态代理（Proxy）+ getAnnotations 家族。

## 易错规范细节（JDK 27）
- toString 用 canonical 名（嵌套类 `A.Rich`）、字符串加引号（`s="x"`）、Class 用 `java.lang.String.class`、数组用 `{"a", "b"}`；与 JDK 8 旧格式（`$` 名、不加引号、`class X`、`[a, b]`）不同。
- 成员顺序实现相关，规范不保证；断言用 contains/前缀。
- 数组成员由代理按需克隆返回。
