# javax.swing.plaf UIResource 包装族 — DESIGN

## 1. API 功能
`UIResourceWrapperContractsTest` 覆盖 plaf 根包 9 个 UIResource 包装类型 + `BorderUIResource` 的 7 个嵌套 Border：ColorUIResource/DimensionUIResource/InsetsUIResource/FontUIResource/IconUIResource/BorderUIResource/InputMapUIResource/ActionMapUIResource/ComponentInputMapUIResource。作用：标记「外观安装的值」，UIManager 依据它区分 LaF 值与用户值。

## 2. Java 用户如何使用
外观实现用 `new ColorUIResource(...)` 等安装默认值；组件 updateUI 后 `getBackground()/getFont()/getBorder()` 返回的即是带 UIResource 标记的对象。包装类的 equals/hashCode 与被包装的 AWT/Swing 原生类型跨实现互通（ColorUIResource equals 普通 Color）。

## 3. 开源用法调研
- `jdk8u-dev/jdk/src/share/classes/javax/swing/plaf/ColorUIResource.java`：四构造器（int/int,int,int/float/Color）。
- `jdk8u-dev/jdk/src/share/classes/javax/swing/plaf/BorderUIResource.java`：BorderUIResource(Border) 委托 + 7 嵌套类。
- `jdk8u-dev/jdk/src/share/classes/javax/swing/ComponentInputMap.java:50`：null component → IllegalArgumentException("ComponentInputMaps must be associated with a non-null JComponent")。
- `jdk8u-dev/jdk/src/share/classes/javax/swing/JComponent.java`：updateUI 清 UIResource 值。

## 4. 测试用例设计 + 自动化实现（76 断言）
- ColorUIResource：4 构造器互通相等（int 打包/float/Color 拷贝）；equals/hashCode 与普通 Color 对称一致；序列化往返保持颜色与 UIResource 标记。
- Dimension/Insets UIResource：与原生 equals/hashCode 一致；可变（setSize/top=）；序列化往返。
- FontUIResource：name/style/size 往返；与同参 Font equals；style/size 差一即不等；Font 拷贝构造保持标记。
- IconUIResource：宽高/绘制委托内层 MetalIconFactory.getCheckBoxIcon()（paintIcon 到 BufferedImage 有像素）。
- BorderUIResource：Border 委托（insets/isBorderOpaque）；7 嵌套类 instanceof UIResource；Empty(1,2,3,4)/Etched 默认 LOWERED(2,2,2,2)/Bevel RAISED(2,2,2,2)/Line(1,1,1,1)+颜色往返/Matte(1,2,3,4)/Compound 内外边和(3,3,3,3)与成员保持/Titled("T") 默认位置 + null 组件 NPE（需 label 度量）。
- InputMap/ActionMap/ComponentInputMap：put/get/remove/parent 遮蔽回落（子删后见父值）/keys 空数组；ComponentInputMap(null) IAE。
- 端到端：Metal 安装后 Label.font、面板 bg/fg/font、border 均为 UIResource。

## 5. 易错规范细节
- BevelBorder 默认 insets 是 2 不是 1；TitledBorder.getBorderInsets(null) 在两侧都 NPE（取 label 度量），断言须传真实组件。
- Metal Ocean 主题 CheckBoxIcon.paintIcon 会把组件 cast 成 JCheckBox，paint 探针必须传 JCheckBox。
