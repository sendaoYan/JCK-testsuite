# java.lang.annotation.IncompleteAnnotationException — 设计方案（JDK 8）

## 1. API 功能
`RuntimeException`：注解类型在编译使用方后新增了无默认值成员，读取该注解时抛出。
- 唯一 public 构造器：`(Class<? extends Annotation> annotationType, String elementName)`
- `annotationType()`、`elementName()`
- `serialVersionUID = 8445097402741811912L`

## 2. Java 用户如何使用
```java
catch (IncompleteAnnotationException e) {
    Class<?> anno = e.annotationType(); // 缺成员的注解类型
    String missing = e.elementName();   // 缺失成员名
}
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationInvocationHandler.invoke`：成员缺失且无默认时抛出。
- JDK `test/jdk/java/lang/annotation/` 双版本类触发路径。
- Spring `AnnotationUtils.getRequiredAttribute` 转译该异常。

## 4. 测试用例设计 + 自动化实现
- 结构：RuntimeException 子类、`annotationType()`/`elementName()` 返回类型、唯一 public 构造器。
- `annotationType()`/`elementName()` 精确身份回读；message 自动生成含 `missing element`。
- **构造器 NPE**：任一参数为 null 抛 NullPointerException。
- 两实例独立；suppression/stackTrace/toString。
- 序列化 round-trip：annotationType + elementName 均存活；serialVersionUID 稳定。

## 易错规范细节（JDK 8）
- 构造器对 null 参数抛 NPE（javadoc 明确）。
- message 使用 `annotationType.getName() + " missing element " + elementName`；不硬编码完整 message（类型名实现相关）。
