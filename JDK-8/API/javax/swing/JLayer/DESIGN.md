# javax.swing.JLayer 测试设计

## 1. API 功能
装饰层容器（final，JComponent + Scrollable/PropertyChangeListener/Accessible）：
包裹一个 view 组件，把绘制/事件拦截委托给 LayerUI；持有 glassPane；
`setLayerEventMask` 决定截获的事件位掩码；setLayout 被禁用。

## 2. Java 用户如何使用
`JLayer<JPanel> l = new JLayer<>(panel, new MyLayerUI()); frame.add(l);`
典型：气泡提示、绘图装饰、事件/焦点审计。`l.setView(newPanel)` 换视图。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JLayer.java`；
`test/jdk/javax/swing/JLayer/`（6730930 等 bug 回归）。

## 4. 测试用例设计 + 易错细节
- 构造器：`new JLayer()`（view=null，UI 是共享 LayerUI 实例）、`new JLayer(view)`、
  `new JLayer(null, ui)`、`new JLayer(view, ui)` 全部合法；getView/getUI 恒等往返。
- view 父子关系：view 的 parent 就是 layer；setView(newView) 后新 view 挂上、
  旧 view parent==null；setView(null) 合法。
- glassPane：默认非 null、是 JPanel、不透明 false；setGlassPane 往返；
  **setGlassPane(null) 合法（getGlassPane()==null）**——与 JRootPane 的 NPE 行为相反。
- LayerUI：getUIClassID()=="ComponentUI"（不是 LayerUI！）；setUI 触发
  propertyName=="UI" 的 PropertyChangeEvent（oldValue==旧 UI、newValue==新 UI）；
  updateUI 不抛。
- setLayerEventMask 默认 0，任意 long 往返。
- **setLayout(non-null)→IllegalArgumentException**（"JLayer.setLayout() not supported"，
  断类型）；setLayout(null) 合法。
- **版本差异（重点）**：JDK-8 `setBorder(non-null)→IllegalArgumentException`
  （"JLayer.setBorder() not supported"，直接拒绝）；JDK-27 起委托给 view
  （`view.setBorder(b)` 生效、getBorder() 读 view 的 border）。两侧各按各自行为断言；
  公共部分：setBorder(null) 合法、getBorder() 无 view 时返回 null。
- 泛型坑：`JLayer<V>.getUI()` 返回 `LayerUI<? super V>`，接收变量要用 `LayerUI<?>`。
- 环境：headless 下 Metal LaF 先装；组件操作 EDT；异常只断类型。

自动化实现位于同目录 `JLayerTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印 `PASS (N assertions)`；
jtreg `@run main` 直接驱动。
