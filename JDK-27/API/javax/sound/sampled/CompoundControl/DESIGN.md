# javax.sound.sampled.CompoundControl — DESIGN

## 1. API 功能
`CompoundControl` 是抽象类，继承 `Control`，表示控制多个相关属性的复合控件（如图形均衡器）。方法分组：
- 构造器（受保护）：`CompoundControl(Type, Control[] memberControls)`。
- 查询：`getMemberControls()`（返回成员数组的 clone）。
- 工具：`toString()`（列出成员控件类型）。

## 2. Java 用户如何使用
用户匿名子类化：`new CompoundControl(type, new Control[]{ gain, pan }) {}`，然后 `getMemberControls()` 遍历成员、`getType().toString()` 得到类型名。

## 3. 开源代码实际用法调研（来源：jdk25u-dev 源码）
- `src/java.desktop/share/classes/javax/sound/sampled/CompoundControl.java:51`：构造器 `this.controls = memberControls`。
- `src/java.desktop/share/classes/javax/sound/sampled/CompoundControl.java:61`：`getMemberControls()` 返回 `controls.clone()`，防止外部修改内部数组。
- `src/java.desktop/share/classes/javax/sound/sampled/CompoundControl.java:71`：`toString()` 用 `StringJoiner` 列出 `[type1, type2]`。

## 4. 测试用例设计 + 自动化实现（`CompoundControlTest.testBlackBox`）
- 匿名子类实例化：类型身份与名字。
- `getMemberControls()`：长度、成员身份保留、返回的是 clone（与传入数组不同引用）。
- 修改返回数组后内部成员不受影响。
- `toString()` 列出成员类型名。
