# javax.swing.colorchooser.ColorChooserComponentFactory 设计案

## 1. API 功能
颜色选择器组件工厂，提供默认选择面板数组与预览面板。

- **构造器**：`private ColorChooserComponentFactory()`（不可实例化）。
- **静态工厂**：`static AbstractColorChooserPanel[] getDefaultChooserPanels()`、`static JComponent getPreviewPanel()`。

## 2. Java 用户如何使用
```java
AbstractColorChooserPanel[] panels = ColorChooserComponentFactory.getDefaultChooserPanels();
JComponent preview = ColorChooserComponentFactory.getPreviewPanel();
```

典型组合链：`getDefaultChooserPanels → 遍历 getDisplayName → getPreviewPanel`（`JColorChooser` 内部 `init` 即如此组装）。

## 3. 开源代码实际用法调研
- `src/java.desktop/share/classes/javax/swing/JColorChooser.java`：字段 `private JComponent previewPanel = ColorChooserComponentFactory.getPreviewPanel();`。
- `src/java.desktop/share/classes/javax/swing/colorchooser/ColorChooserComponentFactory.java`：`getDefaultChooserPanels()` 返回 `{ new DefaultSwatchChooserPanel(), new ColorChooserPanel(new ColorModelHSV()), ... CMYK }`。

## 4. 测试用例设计 + 自动化实现
`ColorChooserComponentFactoryTest.testBlackBox()`（headless 安全，仅构造面板不弹窗）覆盖：
- `getDefaultChooserPanels` 非空、长度 >=1、每项非空且为 `AbstractColorChooserPanel`、`getDisplayName` 非空。
- `getPreviewPanel` 非空且为 `JComponent`。
- 白盒保留结构断言（父类 `Object`、私有唯一构造器、两个静态工厂方法存在）。
