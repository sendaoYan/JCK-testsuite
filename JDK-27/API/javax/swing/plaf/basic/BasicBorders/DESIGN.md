# javax.swing.plaf.basic.BasicBorders

## 1. API 功能

Swing Basic 外观的辅助类。

## 2. Java 用户如何使用

由外观 installUI 时实例化，作为内部实现细节使用。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicBorders.java:48`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/basic/BasicBorders.java:51`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：直接 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，纯数据/接口用例直接在主线程在事件分发线程（EDT）上运行，结束后释放组件。

---

## 契约深化（本轮追加，`BasicBordersContractTest`，38 断言）

- **工厂返回类型**：Button/RadioButton/Toggle/InternalFrame → CompoundBorderUIResource；MenuBar → MenuBarBorder；SplitPane/Divider → SplitPane*Border（Divider 类包私有，按类名后缀断言）；TextField → FieldBorder；ProgressBar → LineBorderUIResource。
- **组件上精确 insets（Metal Ocean）**：ButtonBorder 在新 JButton (4,17,5,17) = border(2,3,3,3)+margin(2,14,2,14)；ToggleButton (4,16,4,16)；RadioButtonBorder 在 JRadioButton (4,4,4,4)、在普通 JButton (4,16,4,16)（margin 宿主差异）；MenuBar (0,0,2,0)；SplitPane/Divider (1,1,1,1) + isBorderOpaque true；ProgressBar (2,2,2,2)；FieldBorder 在 JTextField (2,2,2,2)。
- **嵌套构造器**（protected 4 色/2 色，匿名子类实例化）：ButtonBorder (2,3,3,3) 上不对称（BasicBorders.java 硬编码）；ToggleButtonBorder/RadioButtonBorder 覆写为对称 (2,2,2,2)；MenuBarBorder (0,0,2,0)；FieldBorder (2,2,2,2)；SplitPaneBorder public 构造器 (1,1,1,1)。
- **绘制几何**：MenuBarBorder 40x20 恰 80 px 全在底两行；LineBorder 环画内部不画。

### 版本备注
BasicBorders 双侧行为一致（Metal 版差异在 MetalBorders，见 metal/MetalBorders/DESIGN.md）。
