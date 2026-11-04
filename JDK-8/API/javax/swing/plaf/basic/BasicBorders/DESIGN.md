# javax.swing.plaf.basic.BasicBorders — DESIGN

## 1. API 功能
Basic/Metal 共享的边框套件。`BasicBordersContractTest` 覆盖：8 个静态工厂返回类型、组件上精确 insets、嵌套边框构造器（ButtonBorder/MenuBarBorder/FieldBorder/ToggleButtonBorder/RadioButtonBorder/SplitPaneBorder）、固定几何绘制。与 javax/swing/border 批不重复：只测 plaf 特有的工厂/嵌套类。

## 2. Java 用户如何使用
`BasicBorders.getButtonBorder()` 组合 ETCHED 类边框与组件 margin（EmptyBorder 语义）；安装到按钮后 getBorderInsets = 边框部分 + margin 部分。SplitPaneDividerBorder 是包私有类（经工厂获得）。

## 3. 开源用法调研
- `jdk8u-dev/.../basic/BasicBorders.java:50-131`：八个工厂；ButtonBorder.getBorderInsets 返回 (2,3,3,3)（上不对称）。
- `BasicBorders.java:349`：MenuBarBorder 底缘 2px 线，insets (0,0,2,0)。
- Metal defaults：Button.margin=(2,14,2,14)、ToggleButton 同、RadioButton margin=(2,2,2,2)。

## 4. 测试用例设计 + 自动化实现（38 断言）
- 工厂类型：Button/RadioButton/Toggle/InternalFrame→CompoundBorderUIResource；MenuBar→MenuBarBorder；SplitPane/Divider→SplitPane*Border；TextField→FieldBorder；ProgressBar→LineBorderUIResource；全部（除包私有 Divider）UIResource 标记。
- 组件 insets 精确（Metal Ocean）：ButtonBorder 在新 JButton 上 (4,17,5,17)=border(2,3,3,3)+margin(2,14,2,14)；Toggle (4,16,4,16)；Radio 在 JRadioButton (4,4,4,4)（自带 margin）、在普通按钮 (4,16,4,16)；MenuBar (0,0,2,0)；SplitPane/Divider (1,1,1,1)+opaque；ProgressBar (2,2,2,2)；FieldBorder 在 JTextField (2,2,2,2)。
- 嵌套构造器（4 色/2 色 public，子类实例化）：ButtonBorder (2,3,3,3) 不对称；Menu/Field/Toggle/Radio (2,2,2,2)/(0,0,2,0)；SplitPaneBorder (1,1,1,1) opaque。
- 绘制几何：MenuBarBorder 40x20 恰 80 px 全在底两行；LineBorder 内部不画、环画；CompoundBorder(null 内层) insets 不抛。

## 5. 易错规范细节
- ButtonBorder 上不对称 (2,3,3,3)：ButtonBorder.getBorderInsets 硬编码；ToggleButtonBorder/RadioButtonBorder 覆写为对称 (2,2,2,2)。
- 组合边框读组件 margin：无 margin 组件（JMenuBar/JLabel）与有 margin 组件（JButton/JToggleButton/JRadioButton）insets 不同，断言必须指明宿主组件。
