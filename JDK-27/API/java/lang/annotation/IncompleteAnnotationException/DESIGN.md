# java.lang.annotation.IncompleteAnnotationException — 设计方案（JDK 27）

## 1. API 功能
RuntimeException：注解类型在编译使用方后新增无默认值成员，读取时抛出。
- 唯一 public 构造器：`(Class<? extends Annotation>, String)`
- `annotationType()`、`elementName()`
- `serialVersionUID = 8445097402741811912L`

## 2. Java 用户如何使用
```java
catch (IncompleteAnnotationException e) {
    Class<?> anno = e.annotationType();
    String missing = e.elementName();
}
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationInvocationHandler.invoke`。
- JDK `test/jdk/java/lang/annotation/` 双版本触发。
- Spring `AnnotationUtils.getRequiredAttribute` 转译。

## 4. 测试用例设计 + 自动化实现
白盒：RuntimeException 子类、`annotationType()`/`elementName()` 返回类型、唯一 public 构造器。
黑盒：精确身份回读；message 含 `missing element`；**null 参数抛 NPE**；两实例独立；suppression/stackTrace/toString；对照有默认成员正常读取。
序列化：round-trip 保留 annotationType+elementName；serialVersionUID 稳定。

## 易错规范细节（JDK 27）
- 构造器对 null 参数抛 NPE。
- message = `annotationType.getName() + " missing element " + elementName`。
