# ChangeEvent 行为测试设计

## 1. API 功能
`javax.swing.event.ChangeEvent` 是状态变化事件，继承 `java.util.EventObject`，是最简单的 Swing 事件之一。

- **构造器**：`ChangeEvent(Object source)`。
- **查询**：继承的 `getSource()`、`toString()`（EventObject 实现会嵌入 source 的 `toString()`）。

## 2. Java 用户如何使用
组件内部状态变化时派发，用户注册 `ChangeListener` 读取源：

```java
model.addChangeListener(e -> {
    Object src = e.getSource();      // 变化源，如 JSlider / SpinnerModel
    // 触发重绘 / 刷新 UI
});
```

每个方法典型调用：`new ChangeEvent(source)` 构造 → `getSource()` 回读 → 依源对象类型做后续操作（如 `((SpinnerModel)e.getSource()).getValue()`）。

## 3. 开源代码实际用法调研
1. `test/jdk/java/awt/Component/GetListenersTest.java` 等用例中大量 `ChangeEvent` 作为 listener 测试的载体。
2. JDK 内部 `AbstractSpinnerModel`、`JSlider` 等通过 `fireStateChanged()` 内部 `new ChangeEvent(this)` 派发，是本类最典型的生产用法。

## 4. 测试用例设计 + 自动化实现
- 白盒：保留原结构断言（继承 EventObject、非接口/注解/枚举、public、非 abstract、无接口）。
- 黑盒：`new ChangeEvent(source)` 后断言 `getSource()` 身份；用字符串源断言 `getSource().equals(...)` 与 `toString()` 内嵌源文本，形成构造 → 读取 → 字符串化的组合链。
- 自动化：`testWhiteBox()` + `testBlackBox()`，headless-safe。
