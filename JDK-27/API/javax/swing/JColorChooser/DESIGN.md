# javax.swing.JColorChooser 设计案

## 1. API 功能
`JColorChooser` 是颜色选择组件。方法分组：
- 颜色：`getColor/setColor(Color)/setColor(r,g,b)/setColor(int)`。
- 选择模型：`getSelectionModel/setSelectionModel`。
- 面板：`setPreviewPanel/getPreviewPanel/addChooserPanel/removeChooserPanel/setChooserPanels/getChooserPanels`。
- 拖拽：`setDragEnabled/getDragEnabled`。

## 2. Java 用户如何使用
用户嵌入或弹出颜色选择器：
```java
JColorChooser chooser = new JColorChooser(Color.RED);
Color chosen = chooser.getColor();
```
典型链路：`new JColorChooser(color)` → `setColor/getColor` → `getSelectionModel`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/JColorChooser.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JColorChooser.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `JComponent` + `Accessible`。
- 行为（黑盒，headful）：`testBlackBox()` 覆盖颜色 get/set 三种形式、选择模型、UIClassID、预览/选择器面板 add/remove、拖拽开关，并在 EDT 上 `new JFrame + setVisible(true) + dispose()` 真实显示。

## 5. 追加契约段（2026-11 深化）
ctor 三态（WHITE 默认/Color/model 身份保留且颜色跟随）；事件计数（异色 1、同色 0、模型切换后事件
来自新模型）；Metal 面板计数 5；removeChooserPanel 返回被移除面板；移除未加入面板→IAE；
**重复 add 已安装面板→RuntimeException（面板拒绝二次 install）**；
**getChooserPanels() 每次返回防御拷贝（JDK-27；JDK-8 返回同一内部数组引用——版本差异，各侧各自锁定）**；
setPreviewPanel(null)→null；getUI/updateUI/accessible。
