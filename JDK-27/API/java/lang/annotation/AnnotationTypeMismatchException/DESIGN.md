# java.lang.annotation.AnnotationTypeMismatchException — 设计方案（JDK 27）

## 1. API 功能
RuntimeException：注解元素类型在编译后变化，反射读取时抛出。
- 唯一 public 构造器：`(Method element, String foundType)`
- `element()`、`foundType()`
- `serialVersionUID = 8125925355765570191L`

## 2. Java 用户如何使用
```java
catch (AnnotationTypeMismatchException e) {
    Method m = e.element();   // 哪个成员变了
    String t = e.foundType(); // classfile 实际发现类型描述符
}
```

## 3. 开源代码实际用法调研
- JDK `sun/reflect/annotation/AnnotationType.matchToMethod`。
- JDK `test/jdk/java/lang/annotation/AnnotationTypeMismatchException/` 双版本触发。
- Spring `AnnotationUtils` 透传。

## 4. 测试用例设计 + 自动化实现
白盒：RuntimeException 子类、`element()`/`foundType()` 返回类型、唯一 public 构造器。
黑盒：`element()`/`foundType()` 精确回读；message 前缀 `Incorrectly typed data found for annotation element`；null element/foundType 合法；RuntimeException 捕获；两实例互异；suppression/stackTrace。
序列化：serialVersionUID 稳定；**JDK 27 element 为 transient** → round-trip 后 element()==null、foundType 存活。

## 易错规范细节（JDK 27）
- JDK 27 的 `element` 字段 transient：序列化 round-trip 成功且 `element()` 返回 null（javadoc：序列化后可能不可用）；JDK 8 非 transient，带真实 Method 序列化抛 NotSerializableException。
- message 自动生成，不断言完整 message（含 Method.toString() 实现相关）。
