# java.awt.CheckboxGroup 黑盒行为测试设计

## ① API 功能分组
- 构造：`CheckboxGroup()`。
- 选择：`getSelectedCheckbox/getCurrent/setSelectedCheckbox/setCurrent`。
- 工具：`toString`。

## ② Java 用户用法
```java
CheckboxGroup g = new CheckboxGroup();
Checkbox a = new Checkbox("a", false, g);
Checkbox b = new Checkbox("b", true, g);
g.setSelectedCheckbox(a);
```
组合链路：构造组 → 创建带组 Checkbox → 组选择自动同步 → setSelectedCheckbox/setCurrent 互斥切换。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `CheckboxGroup.setSelectedCheckbox` 设置 `selectedCheckbox` 并同步各 Checkbox 状态（`CheckboxGroup.java`）。
- `CheckboxGroup.getCurrent` 是 `getSelectedCheckbox` 的别名（`CheckboxGroup.java`）。
- `Checkbox` 构造时若 state 为 true 会调用 `group.setSelectedCheckbox(this)`（`Checkbox.java`）。

## ④ 测试用例设计 + 自动化实现
- 初始 selected/current 为 null → `testBlackBox`。
- 构造带组 Checkbox 自动选中 true 项 → `testBlackBox`。
- setSelectedCheckbox/setCurrent 互斥切换 + 状态同步 → `testBlackBox`。
- toString 格式 → `testBlackBox`。

## ⑤ 契约深化（本轮追加）
- 初始无选择；构造为 true 的 Checkbox 加入组即成为 selection。
- `setCurrent`/`setSelectedCheckbox` 互为别名（两侧走查）；自赋值保持选择。
- `setSelectedCheckbox(null)` 取消成员的 state（不是仅清引用）。
- toString 精确格式 `java.awt.CheckboxGroup[selectedCheckbox=...]`，null 选择时输出 selectedCheckbox=null。
- implements Serializable：序列化往返后选择关系保留。
