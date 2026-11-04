# javax.swing.colorchooser.AbstractColorChooserPanel 设计案

## 1. API 功能
`JColorChooser` 选择面板的抽象基类（继承 `JPanel`），定义构建、更新、名称、图标与模型联动。

- **常量**：`String TRANSPARENCY_ENABLED_PROPERTY = "TransparencyEnabled"`。
- **抽象方法**：`updateChooser()`、`buildChooser()`（protected）、`getDisplayName()`、`getSmallDisplayIcon()`、`getLargeDisplayIcon()`。
- **生命周期**：`installChooserPanel(JColorChooser)`（重复安装抛 `RuntimeException`）、`uninstallChooserPanel(JColorChooser)`。
- **模型**：`getColorSelectionModel()`（未安装返回 `null`）、`getColorFromModel()`（protected）。
- **提示**：`getMnemonic()`（默认 0）、`getDisplayedMnemonicIndex()`（默认 -1）、`isColorTransparencySelectionEnabled()`（默认 true，`set...` 基类空实现）、`paint(Graphics)`。

## 2. Java 用户如何使用
子类实现 5 个抽象方法，由 `JColorChooser` 安装：

```java
class TestPanel extends AbstractColorChooserPanel {
    public void updateChooser() { /* 模型变化时刷新 */ }
    protected void buildChooser() { /* 构建子组件 */ }
    public String getDisplayName() { return "RGB"; }
    public Icon getSmallDisplayIcon() { return null; }
    public Icon getLargeDisplayIcon() { return null; }
}
chooser.addChooserPanel(new TestPanel());
```

典型组合链：`new 子类 → installChooserPanel(chooser)（buildChooser+updateChooser）→ getColorSelectionModel → uninstallChooserPanel`。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/colorchooser/ColorChooserComponentFactory.java`：`getDefaultChooserPanels()` 返回 `DefaultSwatchChooserPanel` 与 4 个 `ColorChooserPanel`（HSV/HSL/RGB/CMYK），均为 `AbstractColorChooserPanel` 具体子类。
- `src/java.desktop/share/classes/javax/swing/colorchooser/AbstractColorChooserPanel.java`：`installChooserPanel` 实现为 `chooser = enclosingChooser; chooser.addPropertyChangeListener("enabled", enabledListener); setEnabled(...); buildChooser(); updateChooser();`。

## 4. 测试用例设计 + 自动化实现
`AbstractColorChooserPanelTest.testBlackBox()`（headless 安全，仅构造 `JColorChooser` 不弹窗）覆盖：
- `getDisplayName`/默认 `getMnemonic==0`/`getDisplayedMnemonicIndex==-1`。
- 透明度提示默认 true、`set...` 基类空实现后仍 true。
- 安装前 `getColorSelectionModel()==null` 且回调未触发；安装后 `buildChooser`/`updateChooser` 触发、模型连到 chooser；重复安装抛 `RuntimeException`；卸载后模型为 null。
- 用 `new JColorChooser(Color.red)` 验证模型选中色经面板可见。
- 白盒保留结构断言（父类 `JPanel`、抽象、常量值、5 个抽象方法）。
