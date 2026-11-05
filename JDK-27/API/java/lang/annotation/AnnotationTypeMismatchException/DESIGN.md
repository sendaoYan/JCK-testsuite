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

## 契约深化（2026-11）
### API 功能
- 构造器签名 javap 实测：恰 1 个声明构造器 public (Method, String)，与 JDK-8 相同（公开签名无版本差异）。
- 精确消息组成锁定：`Incorrectly typed data found for annotation element <Method> (Found data of type <foundType>)`，null 形态逐字一致。
- element()/foundType() 反射视图（名称/声明类/静态 Method/含空格串）。

### 使用方式
动态拼期望消息 + 反射视图断言。

### 参考来源
javap（jdk-27.0.1）；运行时行为观察。

### 测试用例设计 + 易错规范细节（本轮新增）
- `testConstructorMatrix` / `testMessageExact` / `testElementView`。
- 版本差异（既有已锁）：element 27 侧 transient → 序列化 round-trip 后 element()==null 而 foundType 存活。
