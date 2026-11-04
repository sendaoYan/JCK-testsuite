# javax.swing.plaf.synth.Region — DESIGN

## 1. API 功能
Synth 样式区域标识：约 60 个静态单例常量 + getName()/isSubregion()/toString()。深化 `RegionTest`：完整常量表（名称、单例、子区域标志）。

## 2. Java 用户如何使用
作为 `SynthStyleFactory.getStyle(JComponent, Region)` 与 `new SynthContext(comp, region, style, state)` 的区域参数；XML `<style id="x"><state .../></style><bind style="x" type="region" key="Button"/>` 里的 key 即 Region 名。

## 3. 开源用法调研
- `jdk8u-dev/.../synth/Region.java:85-426`：全部常量定义（名称串 + isSubregion 布尔）。
- 子区域共 11 个：MENU_ITEM_ACCELERATOR、SCROLL_BAR_TRACK/THUMB、SLIDER_TRACK/THUMB、SPLIT_PANE_DIVIDER、TABBED_PANE_TAB/TAB_AREA/CONTENT、TOOL_BAR_CONTENT、TREE_CELL。
- OpenJDK jtreg `test/javax/swing/plaf/synth/`：Region 绑定回归。

## 4. 测试用例设计 + 自动化实现（310 断言，深化新增 ~295）
- 完整常量表（57 项）：字段可读非空；两次读取 == 单例；getName() 精确串（"ArrowButton"/"Button"/…）；isSubregion() 与文档表一致；toString()==getName()；全部互异（HashSet 计数）。
- 反射核对：公共静态 Region 字段数 == 表长（无文档外常量）。
- 子区域语义：11 个子区域逐一 true；表内恰 11 个 true。
- 保留原白盒 Part（结构断言原文不动）。

## 5. 易错规范细节
- ARROW_BUTTON 的 isSubregion 是 false（虽是按钮的视觉部件，常量表未标子区域）；FORMATTED_TEXT_FIELD 同为 false。
- Region 无公开构造器（protected），自定义区域经子类；toString 就是 name，无前缀。
