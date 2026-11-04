# java.awt.GridBagConstraints 黑盒行为测试设计

## ① API 功能分组
- 构造器：无参默认构造、11 参全字段构造。
- 常量：`RELATIVE/REMAINDER/NONE/BOTH/HORIZONTAL/VERTICAL/CENTER/NORTH/…/WEST`，以及 baseline 系列 `BASELINE/ABOVE_BASELINE/BELOW_BASELINE` 等。
- 公开字段：`gridx/gridy/gridwidth/gridheight/weightx/weighty/anchor/fill/insets/ipadx/ipady`。
- 工具：`clone()`（深拷贝 `insets`）。

## ② Java 用户用法
```java
GridBagConstraints c = new GridBagConstraints();
c.gridx = 0; c.gridy = 0; c.weightx = 1; c.fill = GridBagConstraints.HORIZONTAL;
container.add(comp, c);
GridBagConstraints copy = (GridBagConstraints) c.clone();
```
组合链路：默认构造 → 设置字段 → 传给 `GridBagLayout` → `clone` 复制约束 → 修改副本不影响原约束。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `GridBagLayout.setConstraints(Component, GridBagConstraints)` 调用 `constraints.clone()` 存储（`GridBagLayout.java`）。
- `GridBagLayout.adjustForGravity` 读取 `constraints.anchor/fill/insets`（`GridBagLayout.java`）。
- `GridBagConstraints` 默认构造设置 `gridx=RELATIVE, gridy=RELATIVE, anchor=CENTER, fill=NONE`（`GridBagConstraints.java`）。

## ④ 测试用例设计 + 自动化实现
- 默认构造 11 字段默认值 → `testBlackBox`。
- 常量精确值（RELATIVE=-1、BOTH=1、CENTER=10、ABOVE_BASELINE_LEADING=ABOVE_BASELINE+0x100 等）→ `testBlackBox`。
- 11 参构造全字段回读 → `testBlackBox`。
- `clone` 独立对象 + insets 深拷贝 + 修改副本不影响原对象 → `testBlackBox`。
