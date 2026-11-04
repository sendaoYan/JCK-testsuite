# Cloneable — 设计方案

## 1. API 功能
`java.lang.Cloneable`（接口）是标记接口，声明无任何方法，用于告诉 `Object.clone()` 允许字段级浅拷贝。未实现 `Cloneable` 的类调用 `clone()` 抛 `CloneNotSupportedException`；实现后返回同类的独立实例（浅拷贝）。

## 2. Java 用户如何使用
- 需要 `Object.clone()` 的自定义类声明 `implements Cloneable`，覆写 `public Object clone()`。
```java
class Box implements Cloneable {
  public Object clone() throws CloneNotSupportedException { return super.clone(); }
}
```

## 3. 开源代码实际用法调研
- JDK `test/jdk/java/lang/Cloneable`、`ArrayList.clone()`/数组克隆均依赖标记语义：不实现则 `clone()` 抛异常，实现则浅拷贝。

## 4. 测试用例设计 + 自动化实现
白盒：`Class.forName("java.lang.Cloneable")` 验证为接口且无任何方法（标记接口）。
黑盒：未实现类 `clone()` 抛 `CloneNotSupportedException`；实现类克隆得到不同实例、同类、字段值相等（浅拷贝）。
对应 `<Simple>Test.java` 白盒 + 黑盒部分。
