# java.awt.Component 黑盒行为测试设计

## ① API 功能分组
- 名称/父子：`getName/setName/getParent`。
- 状态：`setEnabled/isEnabled/enable/disable`、`setVisible/isVisible/show/hide/isShowing/isDisplayable`。
- 颜色字体：`setBackground/getBackground/isBackgroundSet`、`setForeground/getForeground`、`setFont/getFont`、`setLocale/getLocale`。
- 几何重载族：`setSize(int,int)/(Dimension)/resize`、`setBounds(int,int,int,int)/(Rectangle)/reshape`、`setLocation(int,int)/(Point)/move`、`size()/location()/bounds()` 弃用别名、`getSize(rv)/getLocation(rv)/getBounds(rv)` 撰写者重载。
- 命中测试：`contains(int,int)/(Point)/inside`、`getComponentAt/locate`。
- 尺寸提示：`setPreferredSize/getPreferredSize/isPreferredSizeSet` 及 min/max 三组（null 清除语义）。
- 光标/输入：`setCursor/getCursor/isCursorSet`（null 回退继承）、`setIgnoreRepaint/getIgnoreRepaint`、`setComponentOrientation/getComponentOrientation`、`setFocusable/isFocusable/isFocusTraversable`、`setFocusTraversalKeysEnabled`、`getAlignmentX/Y`。
- 属性变更：`addPropertyChangeListener/removePropertyChangeListener/getPropertyChangeListeners`、public `firePropertyChange(byte/char/short/long/float/double)`。
- 监听器族：component/focus/key/mouse/mouseMotion/mouseWheel/hierarchy/hierarchyBounds/inputMethod 八族注册/查询/移除 + `getListeners(Class)` 一致性。
- 工具：`isLightweight/isOpaque/getTreeLock/getToolkit/getGraphicsConfiguration/toString`。

## ② Java 用户用法
```java
Component c = new Canvas();
c.setName("w"); c.setEnabled(true); c.setBackground(Color.RED);
c.setBounds(10, 20, 100, 100);
c.setVisible(true);
c.getParent(); c.getSize(); c.getPreferredSize();
```
组合链路：构造子类实例 → add 到显示中的 Frame → 状态/几何/尺寸提示 getter-setter 组合 → 读回断言；监听器注册后 dispatchEvent 合成事件验证同步分发。

## ③ 开源实际用法调研
JDK 自用（来源 `~/git/jdk25u-dev`）：
- `Container.addImpl` 设置子组件 `parent` 字段并触发 addNotify（`Container.java`）。
- `Component.setBounds` 更新 x/y/width/height 并 reshape 原生 peer（`Component.java`）。
- `Component.firePropertyChange` 对相等旧/新值短路不发事件（`Component.java`）。
- Swing/真实项目大量用 `component.setPreferredSize` + `revalidate` 控制布局。

## ④ 测试用例设计 + 自动化实现
- 对齐常量精确值（TOP/CENTER/BOTTOM/LEFT/RIGHT = 0/0.5/1/0/1）→ `testBlackBox`。
- add 到显示 Frame 后 `isDisplayable/isShowing/getParent/getGraphicsConfiguration` → `testBlackBox`。
- 名称/使能/颜色/字体/区域/几何/尺寸提示/光标/朝向/可聚焦 组合 getter-setter 往返 → `testBlackBox`。
- 弃用方法 `disable/enable/hide/show/move/resize/inside` → `testBlackBox`。
- 独立（无父）默认值矩阵：visible=true/showing=false/displayable=false/name=null/x=y=w=h=0/尺寸提示与颜色光标全 unset → `testStandaloneDefaultsAndGeometry`。
- 几何重载族全枚举 + `size()/location()/bounds()` 别名 + rv 撰写者恒等返回并填充 + 每次 get 新拷贝 + 负坐标/零负尺寸原样存储 → `testStandaloneDefaultsAndGeometry`。
- `contains` 边界矩阵（(0,0)/(w-1,h-1) 命中，(w,h)/负值 越界）+ `getComponentAt` 命中自身/未命中 null → `testStandaloneDefaultsAndGeometry`。
- 尺寸提示 setter(null) 清除 isXxxSet；`setCursor(null)` 清除 isCursorSet 并回退默认光标 → `testStandaloneDefaultsAndGeometry`。
- locale 继承：子随父、自有覆盖、setLocale(null) 回退父 → `testLocaleInheritance`。
- PCE 精确载荷：`name|null|first`、`background|java.awt.Color[r=255,g=0,b=0]|...`、font/locale/focusable 同值短路；remove 后停发；null 监听器 no-op；public firePropertyChange 六基本类型重载精确 old/new → `testPropertyChanges`。
- 无障碍状态事件：enable/disable 各发一次 `ACCESSIBLE_STATE_PROPERTY`，状态不变不发（不锁 old/new 值——JDK 对 enable/disable 均发 (null, ENABLED) 属实现怪癖）→ `testAccessibleStateEvents`。
- 监听器族：null 注册 no-op；重复 add 计数 3、单次 remove 只删一个；`getListeners(Class)` 与 typed getter 数量一致 → `testListenerFamilies`。
- 合成事件同步分发：`dispatchEvent(ComponentEvent MOVED/RESIZED)` 精确 id/source/component；MouseEvent 精确 x/y/clickCount → `testSynthesizedDispatch`。

## ⑤ 反例与精确异常
- `applyComponentOrientation(null)` → NPE。
- 无父且无自有 locale 时 `getLocale()` → IllegalComponentStateException（setLocale(null) 本身被接受）。
- `firePropertyChange` 相等值 → 不发事件（行为断言而非异常）。

自动化实现位于同目录 `ComponentTest.java`：`check/checkThrows` 计数断言，`main` 结束打印 `PASS (N assertions)`；@key headful，jtreg DISPLAY=:77 运行，通过 231 断言。
