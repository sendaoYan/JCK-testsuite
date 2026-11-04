# javax.swing.plaf.synth.Region — DESIGN

## 1. API 功能
`Region` 标识 Synth 样式表中一个可设样式区域（public 普通类，非抽象，`extends Object`，无接口）。约 60 个静态常量（`BUTTON`/`LABEL`/`SCROLL_BAR_THUMB` 等）；方法 `getName()`、`isSubregion()`、`toString()`；protected 构造器 `Region(String, String, boolean)` 供自定义区域。来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/Region.java`（class=74、BUTTON=91、LABEL=165）。

## 2. Java 用户如何使用
作为 `SynthStyleFactory.getStyle(JComponent, Region)` 和 `SynthContext` 构造器的区域参数：`new SynthContext(comp, Region.BUTTON, style, state)`；`Region.LABEL.getName()` 得 "Label"；`Region.SCROLL_BAR_THUMB.isSubregion()` 判断子区域。

## 3. 开源用法调研（来源：JDK 源码）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthStyleFactory.java:39`：`public SynthStyle getStyle(JComponent c, Region id)` 文档示例。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthLookAndFeel.java:341`：`getRegion(JComponent)` 按组件类型返回对应 Region。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/synth/SynthContext.java:76`：构造器接收 `Region region`。

## 4. 测试用例设计（黑盒部分，`RegionTest.testBlackBox`）
1. 57 个代表性常量非空且 `getName()` 非空，且全部互不相同（HashSet 去重）。
2. 抽查若干 `getName()` 精确值（ArrowButton/CheckBox/ScrollBar/Viewport 等）。
3. `getName() == toString()`。
4. `isSubregion()`：主区域为 false，子区域（SCROLL_BAR_THUMB/TRACK、SLIDER_THUMB、TREE_CELL、TABBED_PANE_TAB、TOOL_BAR_CONTENT、MENU_ITEM_ACCELERATOR）为 true。
5. 自定义子类 `new Region("MyRegion", "MyRegionUI", false) {}`：`getName()`/`toString()`/`isSubregion()`。

---

## 契约深化（本轮追加，`RegionTest` 常量表部分，310 断言，新增 ~295）

- **完整 57 项常量表**：字段非 null；两次读取 == 单例；getName() 精确串；isSubregion() 与文档表一致；toString()==getName()；HashSet 全互异。
- **反射核对**：公共静态 Region 字段数 == 表长（无文档外常量）。
- **子区域语义**：11 个子区域逐一 true（MENU_ITEM_ACCELERATOR、SCROLL_BAR_TRACK/THUMB、SLIDER_TRACK/THUMB、SPLIT_PANE_DIVIDER、TABBED_PANE_TAB/TAB_AREA/CONTENT、TOOL_BAR_CONTENT、TREE_CELL）；表内恰 11 个 true。
- 保留原 testBlackBox 结构断言不动。

### 版本备注
Region 常量表双侧（JDK-8/JDK-27）完全一致（57 常量、11 子区域）。
