# java.lang.annotation.IncompleteAnnotationException — 设计方案

## 1. API 功能
RuntimeException：注解类型在编译使用方之后新增了无默认值的成员，读取该注解时抛出：
- 构造：`(Class<? extends Annotation> annotationType, String elementName)`
- 查询：`annotationType()`、`elementName()`

## 2. Java 用户如何使用
```java
catch (IncompleteAnnotationException e) {
    Class<?> anno = e.annotationType();  // 缺成员的注解类型
    String missing = e.elementName();    // 缺失成员名
}
```

## 3. 开源代码实际用法调研
- JDK `src/java.base/share/classes/sun/reflect/annotation/AnnotationInvocationHandler.java`：`invoke` 对 memberValues 缺项且无默认时抛出。
- JDK `test/jdk/java/lang/annotation/`（AnnotationType 相关）验证双版本类触发路径。
- Spring `AnnotationUtils.getRequiredAttribute` 家族对该异常做转译包装（`IllegalArgumentException`）。

## 4. 测试用例设计 + 自动化实现
白盒：RuntimeException 子类、`annotationType()`/`elementName()` 返回类型、public 构造器。
黑盒（真实触发需“旧使用类+新注解定义”双编译产物，单文件内按规范直接构造）：
- `annotationType()`/`elementName()` 精确身份回读；
- message 自动生成且包含成员名；RuntimeException 捕获路径；
- 两实例独立；suppression/stackTrace/toString 机制；
- 对照：有默认成员（Retention.value）正常读取不抛。
对应 `IncompleteAnnotationExceptionTest.java` 的 `testBlackBox()`。
