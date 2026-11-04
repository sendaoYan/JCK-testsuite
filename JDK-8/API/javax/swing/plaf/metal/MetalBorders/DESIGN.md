# javax.swing.plaf.metal.MetalBorders — DESIGN

## 1. API 功能
Metal 边框套件。`MetalBordersContractTest`：5 个公共静态工厂（getButtonBorder/getTextBorder/getTextFieldBorder/getToggleButtonBorder/getDesktopIconBorder）类型与缓存语义、嵌套边框类精确 insets、UIResource 标记与继承关系。

## 2. Java 用户如何使用
工厂组合 Metal 边框与 margin（与 BasicBorders 同构）；嵌套类（ButtonBorder/Flush3DBorder/InternalFrameBorder/…共 14 个 public）均有公共无参构造器，可单独实例化。

## 3. 开源用法调研
- `jdk8u-dev/.../metal/MetalBorders.java`：公共工厂五枚（getToolBarRolloverBorder/Nonrollover 为包私有，JDK-27 强封装下测试不可达）。
- JDK-8 实测 insets（Ocean 主题）：ButtonBorder (3,3,3,3)、Flush3D (2,2,2,2)、InternalFrame (5,5,5,5)、MenuBar (0,0,2,0)、ToolBar(水平) (1,16,3,2)、MenuItem (2,2,2,2)、PopupMenu (3,1,2,1)、RolloverButton (3,3,3,3)、ScrollPane (1,1,2,2)、TableHeader (2,2,2,0)、TextField (2,2,2,2)、Metal ToggleButton (3,3,3,3)、OptionDialog (3,3,3,3)、Palette (1,1,1,1)。

## 4. 测试用例设计 + 自动化实现（35 断言）
- 工厂：五个返回 CompoundBorderUIResource/UIResource；getButtonBorder 二次调用同实例（缓存）。
- 工厂 insets（Metal Ocean）：Button 在 JButton (5,17,5,17)=border(3,3,3,3)+margin(2,14,2,14)；Toggle 同；TextField 在 JTextField (2,2,2,2)；TextBorder 在按钮宿主 (4,16,4,16)。
- 嵌套 14 类精确 insets（见上表；JDK-8 侧 InternalFrameBorder (5,5,5,5)、TableHeaderBorder (2,2,2,0) 为版本差异点）。
- 类型面：TextFieldBorder extends Flush3DBorder；Toggle/Rollover extends ButtonBorder；TableHeaderBorder 非 UIResource；ToolBarBorder implements SwingConstants；复合边框 insets=内外和。

## 5. 版本差异（双侧实测锁定）
- InternalFrameBorder：JDK-8 (5,5,5,5) vs JDK-27 (4,4,4,4)（JDK-27 每边减 1）。
- TableHeaderBorder：JDK-8 (2,2,2,0)（右边留给表头 UI）vs JDK-27 (2,2,2,2)（对称化）。
- 其余 12 类与工厂语义双侧一致。

## 6. 易错规范细节
- ToolBarBorder insets 依赖工具栏方向（水平 (1,16,3,2)）；断言用默认水平向。
- 覆盖 Metal 边框断言前先 setLookAndFeel(Metal)+updateUI，否则组件 margin 未装。
