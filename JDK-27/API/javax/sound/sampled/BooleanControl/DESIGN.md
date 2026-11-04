# javax.sound.sampled.BooleanControl — DESIGN

## 1. API 功能
`BooleanControl` 是抽象类，继承 `Control`，表示在 true/false 两个状态间切换的控件（如静音）。方法分组：
- 构造器（受保护）：`BooleanControl(Type, boolean initialValue, String trueLabel, String falseLabel)`、`BooleanControl(Type, boolean initialValue)`（标签默认 "true"/"false"）。
- 值操作：`setValue(boolean)`、`getValue()`。
- 标签：`getStateLabel(boolean)`。
- 工具：`toString()`（含当前状态标签）。

## 2. Java 用户如何使用
用户通过具体子类匿名实例化：`new BooleanControl(BooleanControl.Type.MUTE, true, "on", "off") {}`，然后 `getType()` → `getValue()` → `setValue(false)` → `getStateLabel(false)`。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/BooleanControl.java:86`：双参构造器默认标签 `"true"/"false"`。
- `src/java.desktop/share/classes/javax/sound/sampled/BooleanControl.java:117`：`getStateLabel` 按状态返回对应标签。
- `src/java.desktop/share/classes/javax/sound/sampled/BooleanControl.java:126`：`toString` 格式 `"%s with current value: %s"`。

## 4. 测试用例设计 + 自动化实现（`BooleanControlTest.testBlackBox`）
- 标签构造器：类型/初始值/`getStateLabel(true/false)` 往返，`setValue(false/true)` 翻转，`toString()` 反映当前标签。
- 默认标签构造器：初始值、默认 "true"/"false" 标签。
