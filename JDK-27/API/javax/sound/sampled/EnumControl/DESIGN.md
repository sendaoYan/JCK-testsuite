# javax.sound.sampled.EnumControl — DESIGN

## 1. API 功能
`EnumControl` 是抽象类，继承 `Control`，表示在一组离散对象值间选择的控件。方法分组：
- 构造器（受保护）：`EnumControl(Type, Object[] values, Object value)`。
- 值操作：`setValue(Object)`（不支持的值抛 `IllegalArgumentException`）、`getValue()`。
- 取值集合：`getValues()`（返回 clone）。
- 工具：`toString()`（含当前值）。

## 2. Java 用户如何使用
用户匿名子类化：`new EnumControl(EnumControl.Type.REVERB, new Object[]{ "a", "b", "c" }, "b") {}`，然后 `getValues()` 遍历可选值、`setValue("c")` 切换、对不支持值抛异常。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/EnumControl.java:87`：`setValue` 经 `isValueSupported` 校验，失败抛 `IllegalArgumentException("Requested value ... is not supported.")`。
- `src/java.desktop/share/classes/javax/sound/sampled/EnumControl.java:109`：`getValues()` 返回 `values.clone()`。
- `src/java.desktop/share/classes/javax/sound/sampled/EnumControl.java:138`：`toString()` 格式 `"%s with current value: %s"`。

## 4. 测试用例设计 + 自动化实现（`EnumControlTest.testBlackBox`）
- 匿名子类实例化：类型/初始值/`getValues()` 长度与顺序、返回 clone。
- `setValue` 往返（含回到原值）。
- `setValue` 不支持值抛 `IllegalArgumentException` 且当前值不变。
- 修改 `getValues()` 返回数组后内部值不受影响。
- `toString()` 含当前值。
