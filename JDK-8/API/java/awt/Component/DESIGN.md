# java.awt.Component 黑盒行为测试设计（JDK-8）

## 1. API 功能
AWT 组件基类（abstract）：名称/父子、状态（enable/visible/showing/displayable）、颜色字体 locale、
几何重载族（setSize/setBounds/setLocation 各 int+对象 形态与 resize/reshape/move/size/location/bounds 弃用别名、
rv 撰写者重载）、contains/inside/getComponentAt 命中、尺寸提示三组（null 清除）、光标（null 继承回退）、
朝向/可聚焦/焦点遍历键开关、对齐常量、PCE 属性变更（含 public firePropertyChange 六基本类型重载）、
八族监听器注册与 getListeners 一致性、dispatchEvent 合成分发。

## 2. Java 用户如何使用
`Component c = new Canvas(); c.setBackground(Color.RED); c.setBounds(10,20,100,100);`
frame.add(c) 后 isDisplayable/isShowing 生效；`c.addPropertyChangeListener(...)` 观察 name/background/font 变更；
`c.dispatchEvent(new ComponentEvent(...))` 同步送达监听器。

## 3. 开源实际用法调研（本地源码，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Component.java`
（setBounds 存储几何并 reshape peer；firePropertyChange 相等值短路；getLocale 无父无自有 locale 抛 ICSE）。

## 4. 测试用例设计 + 易错细节
- 结构反射（保留原断言全集，getMethods 含弃用 getPeer/postEvent/mouseDown 家族）→ `testStructure`。
- 显示 Frame 内组合往返 + 弃用方法 enable/disable/hide/show → `testBlackBox`。
- 独立默认值矩阵 + 几何重载族 + contains 边界（(0,0)/(w-1,h-1) 命中、(w,h) 越界）+ 零负尺寸原样存储 +
  尺寸提示 setter(null) 清除 + setCursor(null) 回退默认 → `testStandaloneDefaultsAndGeometry`。
- locale 继承/覆盖/回退 → `testLocaleInheritance`。
- PCE 精确载荷 `name|null|first`、`background|java.awt.Color[r=255,g=0,b=0]|...`、同值短路、remove 停发、
  firePropertyChange(byte/char/short/long/float/double) 精确 old/new → `testPropertyChanges`。
- 无障碍状态事件（enable/disable 各一次 ACCESSIBLE_STATE_PROPERTY；不锁 old/new——JDK 实现怪癖）→ `testAccessibleStateEvents`。
- 监听器族：null no-op、重复 add 计数、单 remove 删一个、getListeners 与 typed getter 一致 → `testListenerFamilies`。
- dispatchEvent 同步分发 ComponentEvent/MouseEvent 精确 id/source/x/y/clickCount → `testSynthesizedDispatch`。
- 反例：`applyComponentOrientation(null)`→NPE；无父 `getLocale()`→IllegalComponentStateException
  （**易错**：setLocale(null) 本身被接受，抛点在 getLocale）。
- JDK-8 侧 358 断言通过（DISPLAY=:77，@key headful）。
