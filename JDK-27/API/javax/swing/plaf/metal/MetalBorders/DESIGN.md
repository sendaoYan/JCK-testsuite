# javax.swing.plaf.metal.MetalBorders — DESIGN

## 1. API 功能
Metal 边框工厂：`getButtonBorder`/`getTextBorder`/`getTextFieldBorder`/`getToggleButtonBorder`/`getDesktopIconBorder`。

## 2. Java 用户如何使用
由 `MetalLookAndFeel` 提供组件边框；测试直接调用五个静态工厂并断言非空与类型。

## 3. 开源代码实际用法调研（来源：JDK 源码 jdk25u-dev + test/jdk）
来源：JDK 源码 `src/java.desktop/share/classes/javax/swing/plaf/metal/MetalBorders.java`（jdk25u-dev）。

## 4. 测试用例设计 + 自动化实现（`MetalBordersTest.testBlackBox`）
五个静态工厂均返回非空 Border；getButtonBorder 返回 ButtonBorder；构造 MetalBorders 实例。

---

## 契约深化（本轮追加，`MetalBordersContractTest`，35 断言）

- **5 个公共工厂**：getButtonBorder → CompoundBorderUIResource 且缓存实例（二次调用 ==）；getTextBorder → CompoundBorderUIResource；getTextFieldBorder/getToggleButtonBorder/getDesktopIconBorder 均 UIResource。（getToolBarRolloverBorder/NonrolloverBorder 为包私有，JDK-27 强封装下不可达，不断言。）
- **工厂 insets（Metal Ocean）**：ButtonBorder 在 JButton (5,17,5,17) = border(3,3,3,3)+margin(2,14,2,14)；ToggleButton 同；TextFieldBorder 在 JTextField (2,2,2,2)；getTextBorder 在按钮宿主 (4,16,4,16)。
- **14 个嵌套类精确 insets**：ButtonBorder (3,3,3,3)、Flush3DBorder (2,2,2,2)、**InternalFrameBorder (4,4,4,4)**、MenuBarBorder (0,0,2,0)、ToolBarBorder 水平 (1,16,3,2)、MenuItemBorder (2,2,2,2)、PopupMenuBorder (3,1,2,1)、RolloverButtonBorder (3,3,3,3)、ScrollPaneBorder (1,1,2,2)、**TableHeaderBorder (2,2,2,2)**、TextFieldBorder (2,2,2,2)、ToggleButtonBorder (3,3,3,3)、OptionDialogBorder (3,3,3,3)、PaletteBorder (1,1,1,1)。
- **类型面**：TextFieldBorder extends Flush3DBorder；ToggleButtonBorder/RolloverButtonBorder extends ButtonBorder；TableHeaderBorder 非 UIResource；ToolBarBorder implements SwingConstants；复合边框 insets = 内外和。

### 版本差异（双侧实测 + javap 锁定）
- InternalFrameBorder：**JDK-8 (5,5,5,5) → JDK-27 (4,4,4,4)**（每边减 1）。
- TableHeaderBorder：**JDK-8 (2,2,2,0)（右边留给表头 UI）→ JDK-27 (2,2,2,2)**（对称化）。
- 其余 12 类与工厂语义双侧一致。测试内以注释标注差异来源。
