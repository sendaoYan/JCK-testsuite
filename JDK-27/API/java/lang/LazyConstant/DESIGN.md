# LazyConstant — 设计方案

## 1. API 功能
`java.lang.LazyConstant`（预览 API，JDK 25+）是惰性求值的常量容器：`interface LazyConstant<T> extends Supplier<T>`，抽象成员 `T get()`、`equals/hashCode/toString`，静态工厂 `static <T> LazyConstant<T> of(Supplier<T> supplier)`。语义：supplier 仅在被首次 `get()` 时求值一次，之后缓存结果。

## 2. Java 用户如何使用
- 用 `LazyConstant.of(() -> 昂贵计算())` 声明延迟初始化常量，需要时 `get()` 触发求值并缓存。
```java
LazyConstant<String> name = LazyConstant.of(() -> loadName());
name.get(); // 首次求值
name.get(); // 直接返回缓存
```
- 预览 API，编译运行需 `--enable-preview`。

## 3. 开源代码实际用法调研
- JEP 496（`java.lang` 预览）引入 `LazyConstant`；JDK `test/jdk/java/lang/LazyConstant` 验证惰性、单次求值与 `equals/hashCode/toString` 契约。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.LazyConstant")` 验证为接口、继承 `Supplier`、`of(Supplier)` 静态。
黑盒：`of` 不求值 supplier，`get()` 触发并只调用一次（计数），`get` 返回缓存值；验证 `equals(self)`、`hashCode` 稳定、`toString` 非空。
对应 `<Simple>Test.java` 白盒 + 黑盒部分（文件使用 `@enablePreview`）。
