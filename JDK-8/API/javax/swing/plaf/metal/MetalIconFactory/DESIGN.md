# javax.swing.plaf.metal.MetalIconFactory — DESIGN

## 1. API 功能
Metal 图标套件工厂。`MetalIconFactoryContractTest`：24 个公共静态工厂非空 + 精确尺寸、参数化尺寸图标、主题相关滑块图标、无字体几何绘制。

## 2. Java 用户如何使用
`MetalIconFactory.getCheckBoxIcon()` 等直接取图标；`getInternalFrameCloseIcon(size)` 按控件尺寸取；`getTreeControlIcon(boolean isCollapsed)` 展开态区分；DARK/LIGHT 布尔常量供 TreeControlIcon 换色。

## 3. 开源用法调研
- `jdk8u-dev/.../metal/MetalIconFactory.java:92-255`：全工厂清单；DARK=false、LIGHT=true。
- 尺寸表（Ocean 主题实测）：radio/checkbox 13x13、treeFolder 16x18、treeLeaf 16x20、treeComputer/Hard/Floppy 16x16、fileChooser* 18x18、menuArrow/menuItemArrow 4x8、checkbox/radioMenuItem 10x10、treeControl 18x18、滑块 thumb Ocean 15x16/16x15。
- 滑块 thumb 图标类随主题换（OceanHorizontalSliderThumbIcon vs HorizontalSliderThumbIcon）。

## 4. 测试用例设计 + 自动化实现（111 断言）
- 固定尺寸 17 项精确断言 + DARK/LIGHT。
- 参数化：InternalFrame{Close,Maximize,Minimize,AltMaximize}Icon(12/16/20) 尺寸随参数；TreeControlIcon(false/true) 18x18。
- 主题相关：Ocean 下滑块 thumb 15x16/16x15；切 Steel 后仍 15x16/16x15 但实例不同（主题驱动新图标）；finally 恢复 Ocean。
- 绘制几何：checkboxIcon 13x13 全 169 px 画满（背景+框）；InternalFrameCloseIcon(16)（传 JButton 组件）>0；treeLeaf >0。

## 5. 易错规范细节
- InternalFrame*Icon.paintIcon 把组件强转 JButton、CheckBoxIcon（Ocean）强转 JCheckBox——绘制探针组件类型必须匹配。
- 滑块 thumb 图标实例随主题重建，断言「尺寸稳定 + 实例更换」而非类名。
