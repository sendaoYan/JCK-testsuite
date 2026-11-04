# javax.swing.JTabbedPane 黑盒行为测试设计

## 1. API 功能
JTabbedPane 是选项卡容器，继承 JComponent，实现 Serializable、Accessible、SwingConstants，内部用 SingleSelectionModel 管理选中页。

- 构造器：`JTabbedPane()`、`JTabbedPane(int tabPlacement)`、`JTabbedPane(int tabPlacement, int tabLayoutPolicy)`；默认 TOP + WRAP_TAB_LAYOUT。
- 选项卡：`addTab(String, Component)`、`addTab(String, Icon, Component)`、`insertTab(...)`、`removeTabAt`、`getTabCount`。
- 标题/组件/图标：`getTitleAt/setTitleAt`、`getComponentAt/setComponentAt`、`getIconAt/setIconAt`、`getDisabledIconAt/setDisabledIconAt`。
- 索引：`indexOfTab(String/Icon)`、`indexOfComponent`。
- 选择：`getSelectedIndex/setSelectedIndex`、`getSelectedComponent/setSelectedComponent`（找不到组件抛 IAE）。
- 页属性：`setEnabledAt/isEnabledAt`、`setMnemonicAt/getMnemonicAt`、`setToolTipTextAt/getToolTipTextAt`、`setBackgroundAt/getBackgroundAt`、`setForegroundAt/getForegroundAt`。
- 布局：`getTabPlacement/setTabPlacement`、`getTabLayoutPolicy/setTabLayoutPolicy`（非法策略抛 IAE）。

## 2. Java 用户如何使用
- `JTabbedPane tp = new JTabbedPane(); tp.addTab("编辑", editorPanel); tp.addTab("预览", previewPanel);` 添加选项卡。
- `tp.setSelectedIndex(0);` 切换选中；`tp.setEnabledAt(1, false);` 禁用某页。
- `tp.setMnemonicAt(0, KeyEvent.VK_E);` 设置快捷键助记符。
- `tp.indexOfTab("预览")` 反查索引；`tp.setTitleAt(0, "新标题")` 改名。

## 3. 开源代码实际用法调研
JDK 自带测试（`~/git/jdk25u-dev/test/jdk`）：
- `test/jdk/java/awt/a11y/AccessibleJTabbedPaneTest.java` 用 `new JTabbedPane()` + `addTab("Tab 1", panel1)` 做无障碍断言。
- `test/jdk/java/awt/Container/FindComponentTest.java` 用 JTabbedPane 做组件查找。
- `test/jdk/java/awt/image/multiresolution/MultiresolutionIconTest.java` 用 JTabbedPane 展示多分辨率图标。
以上来源为 `~/git/jdk25u-dev/test/jdk`。

## 4. 测试用例设计 + 自动化实现
黑盒场景（`JTabbedPaneTest.testBlackBox()`）：
1. 默认值：tabCount 0 / selectedIndex -1 / selectedComponent null / TOP / WRAP。
2. addTab 首项自动选中、标题/组件往返。
3. indexOfTab/indexOfComponent（含缺失返回 -1）。
4. setSelectedIndex/getSelectedComponent/setSelectedComponent（未知组件 IAE）。
5. setTitleAt 改名后 indexOfTab 生效。
6. setEnabledAt/isEnabledAt、setMnemonicAt/getMnemonicAt（默认 -1）。
7. setToolTipTextAt/setBackgroundAt/setForegroundAt 往返。
8. setComponentAt 替换组件。
9. setIconAt/getIconAt/indexOfTab(Icon)。
10. setTabPlacement/setTabLayoutPolicy 往返 + 非法策略 IAE。
11. removeTabAt 缩减并移位。
