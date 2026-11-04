# javax.swing.plaf.metal.MetalLookAndFeel — DESIGN

## 1. API 功能
`MetalLookAndFeel` 是 Metal（也称 Java 默认）观感（`extends BasicLookAndFeel`，非抽象）。它提供 `getDefaults()` 统一默认值表、`getDescription/getID/getName/isNativeLookAndFeel/isSupportedLookAndFeel`、以及静态 `setCurrentTheme(MetalTheme)/getCurrentTheme()` 主题切换。本测试不通过 `setLookAndFeel` 安装，只直接调用实例方法以避免全局状态污染。

## 2. Java 用户如何使用
`MetalLookAndFeel laf = new MetalLookAndFeel(); UIDefaults d = laf.getDefaults(); MetalLookAndFeel.setCurrentTheme(new OceanTheme());`。主题切换影响后续创建的组件外观。

## 3. 开源用法调研（来源：JDK 源码 + test/jdk）
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:108`：`public class MetalLookAndFeel extends BasicLookAndFeel`。
- `~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java:1625`：`public static void setCurrentTheme(MetalTheme theme)`。
- `~/git/jdk25u-dev/test/jdk/javax/swing/plaf/metal/MetalLookAndFeel/Test8039750.java:38`：`UIDefaults table = new MetalLookAndFeel().getDefaults();`。

## 4. 测试用例设计（黑盒部分，`MetalLookAndFeelTest.testBlackBox`）
1. `getDescription/getID/getName` 非空；`isNativeLookAndFeel()` 为假；`isSupportedLookAndFeel()` 为真。
2. `getDefaults()` 返回非空且包含 `"ComboBoxUI"` 等关键键。
3. `setCurrentTheme`/`getCurrentTheme` 往返无异常（不安装观感）。

---

## 契约深化（本轮追加，`MetalLookAndFeelTest` 黑盒部分 127 断言，新增 ~110）

保留原结构断言，追加：
- **元数据精确**：getName/getID == "Metal"、getDescription 非空、isNativeLookAndFeel false、isSupportedLookAndFeel true、getSupportsWindowDecorations true。
- **主题注册表**：getCurrentTheme 默认 OceanTheme；DefaultMetalTheme/OceanTheme 的 getName "Steel"/"Ocean"；`setCurrentTheme(null)` → NPE 且消息精确 `"Can't have null theme"`；切 Steel getControl==(204,204,204)、controlText==(0,0,0) → 切 Ocean getControl==(238,238,238)（finally 恢复 Ocean）。
- **静态颜色/字体面**（反射循环）：6 个字体 getter 非 null；约 40 个颜色 getter 非 null 且 alpha==255；Steel 黑 (0,0,0) vs Ocean 黑 (51,51,51)（PrintColorUIResource 打印黑）。
- **安装态行为**：UIManager 安装后 defaults 表 >100；ButtonUI 映射 MetalButtonUI；按钮委托为 MetalButtonUI；getDisabledIcon/getDisabledSelectedIcon 对普通图标返回 null；getLayoutStyle(null) 非 null；provideErrorFeedback(null) 容忍。

### 版本备注
双侧（JDK-8/JDK-27）元数据与主题语义一致；JDK-27 源码锚点 `~/git/jdk27/src/java.desktop/share/classes/javax/swing/plaf/metal/MetalLookAndFeel.java`。
