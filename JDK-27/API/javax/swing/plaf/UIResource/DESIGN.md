# javax.swing.plaf.UIResource

## 1. API 功能

标记 javax.swing.plaf.UIResource 接口的资源包装类，供外观实现标记可替换的默认资源。

## 2. Java 用户如何使用

由外观实现构造后放入 UIDefaults，供 UIManager.get 取出复用。

## 3. 开源代码实际用法调研

以 OpenJDK 源码（JDK-27 桌面模块 java.desktop）为准：

- 定义位置：`src/java.desktop/share/classes/javax/swing/plaf/UIResource.java:66`
- 实际引用位置：`src/java.desktop/share/classes/javax/swing/plaf/UIResource.java:42`

## 4. 测试用例设计 + 自动化实现

- 白盒（testWhiteBox）：通过 Class.forName 反射校验类的可加载性、修饰符、父类与实现的接口；
- 黑盒（testBlackBox）：直接 实例化（或经工厂/反射获取）并组合调用关键方法，断言返回值与副作用；
- 自动化：采用 jtreg 单文件测试，纯数据/接口用例直接在主线程在事件分发线程（EDT）上运行，结束后释放组件。

---

## 契约深化（本轮追加，`UIResourceWrapperContractsTest`，76 断言）

- **ColorUIResource**：四构造器（int 打包 / r,g,b / float[] / Color 拷贝）互通相等；equals/hashCode 与普通 Color 对称一致；序列化往返保持颜色与 UIResource 标记。
- **DimensionUIResource / InsetsUIResource**：与原生 Dimension/Insets equals/hashCode 一致；可变（setSize/top=）；序列化往返。
- **FontUIResource**：name/style/size 三参构造往返、与同参 Font equals、style/size 差一即不等、Font 拷贝构造保持标记。
- **IconUIResource**：宽高委托内层（MetalIconFactory.getCheckBoxIcon()），paintIcon 到 BufferedImage 有像素（组件传 JCheckBox，Ocean CheckBoxIcon 会强转）。
- **BorderUIResource**：Border 委托（insets/isBorderOpaque）；7 个嵌套类全 instanceof UIResource，insets 锁定：Empty(1,2,3,4)、Etched 默认 LOWERED (2,2,2,2)、Bevel RAISED (2,2,2,2)、Line(1,1,1,1)+颜色往返、Matte(1,2,3,4)、Compound 内外和 + 成员保持、Titled 默认位置 DEFAULT 且 `getBorderInsets(null)` NPE（需 label 度量）。
- **InputMapUIResource / ActionMapUIResource**：put/get/remove/parent 遮蔽回落（子删后见父值）/keys 空数组；ActionMap 值须为 javax.swing.Action（否则 NPE 由 InputMap.allKeys 触发层接管）。
- **ComponentInputMapUIResource(null)** → IllegalArgumentException（ComponentInputMap 构造前置校验）。
- **端到端**：Metal 安装后 Label.font、panel bg/fg/font、border 均带 UIResource 标记。

### 版本备注
UIResource 族双侧（JDK-8/JDK-27）类型面与行为一致，无版本差异断言分支。
