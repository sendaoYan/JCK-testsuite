# javax.swing.JSeparator 测试设计

## 1. API 功能
分隔线组件（JComponent + SwingConstants + Accessible）：只有 HORIZONTAL=0/
VERTICAL=1 一个方向属性（默认水平）、不可聚焦；getUIClassID=="SeparatorUI"；
其余全是继承自 JComponent 的通用属性。

## 2. Java 用户如何使用
菜单组分隔（`menu.add(new JSeparator())`）与工具栏分组：
`toolBar.add(new JSeparator(SwingConstants.VERTICAL))`。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JSeparator.java`；
JMenu/JToolBar 内部使用；`test/jdk/javax/swing/JSeparator/`。

## 4. 测试用例设计 + 易错细节
- 默认契约：HORIZONTAL、isFocusable()==false（分隔线不参与焦点循环）、
  getUIClassID=="SeparatorUI"、Metal→MetalSeparatorUI、accessible/insets 非 null。
- 方向矩阵：VERTICAL 构造器；setOrientation 双向往返；**setOrientation(99) 与
  new JSeparator(99) 都→IllegalArgumentException**（构造器/setter 双反例）。
- **setOrientation 触发 propertyName=="orientation" 事件**（newValue==新方向值）。
- 通用属性：min/max/preferred size、enabled、tooltip（含 null）、opaque、name
  往返。
- 环境：headless 下 Metal LaF 先装；EDT 操作。

自动化实现位于同目录 `JSeparatorTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印
`PASS (N assertions)`；jtreg `@run main` 直接驱动。
