# javax.swing.JTabbedPane 测试设计

## 1. API 功能
多页签容器（JComponent + Serializable/Accessible/SwingConstants）：单选模型驱动的
tab 集合（addTab/insertTab/removeTabAt/removeAll）、每 tab 的
title/icon/tip/component/enabled/mnemonic/background/foreground 属性、placement
（TOP/BOTTOM/LEFT/RIGHT）与 layout policy（WRAP/SCROLL）两种布局策略、
ChangeListener 通知，UI 委托（getUIClassID=="TabbedPaneUI"，Metal→MetalTabbedPaneUI）。

## 2. Java 用户如何使用
`JTabbedPane tp = new JTabbedPane(); tp.addTab("Main", panel); tp.setSelectedIndex(0);`
向导/设置页/浏览器页签；`tp.add("C", comp)`（Container.add(String,Component) 路由成标题页签）；
`tp.getModel().setSelectedIndex(i)` 与 pane 双向同步。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/JTabbedPane.java`；
`test/jdk/javax/swing/JTabbedPane/`；IDE/NetBeans 设置对话框典型用法。

## 4. 测试用例设计 + 易错细节
- 默认契约：tabCount=0/selectedIndex=-1/placement=TOP/policy=WRAP/tabRunCount=0/
  model 默认 DefaultSingleSelectionModel 且 selected=-1；空 pane 上 indexOfTab(String/Icon/
  Component) 全 -1，indexOfTab((String)null) 也是 -1（null 标题按找不到处理，不抛）。
- 构造器 `new JTabbedPane(BOTTOM, SCROLL_TAB_LAYOUT)` 两参均生效。
- 增查改：addTab 四参/两参；getComponentAt 的 view 的 parent 就是 pane 本身；
  setTitleAt/setToolTipTextAt/setIconAt/setDisabledIconAt/setEnabledAt 全往返（Icon 用固定
  尺寸匿名实现，避免资源依赖）；insertTab(…,0) 插入后原 tab 后移；removeTabAt 后回移；
  add(String,Component) 走 addImpl 变成标题页签。
- **removeAll 后 selectedIndex==-1；再 addTab 第一个自动选中（选中恢复到 0）**。
- 选中/事件语义：首个 addTab 自动选中 0；ChangeListener 只在真正变化时触发
  （重复 setSelectedIndex(同值) 不再触发）；pane↔model 双向同步（model.setSelectedIndex
  同样触发 pane 上的 listener）。
- **删除选中 tab 的迁移规则**（JDK-8/27 一致）：删非末位选中 tab → 选中索引保持
  （邻居内容顶上）；删末位选中 tab → 退到前一个索引。
- 助记符：getMnemonicAt 默认 -1；setMnemonicAt('T') 往返；
  **setDisplayedMnemonicIndexAt(0,0) 只有 title 对应字符与 mnemonic 匹配才成功**。
- 反例矩阵（精确异常类型）：getTitleAt/setTitleAt/getComponentAt/setEnabledAt/
  setSelectedIndex/removeTabAt/setMnemonicAt/setDisplayedMnemonicIndexAt 越界（5 或 -1）
  →IndexOutOfBoundsException；setSelectedIndex(-2) 同抛，但 **setSelectedIndex(-1) 合法**
  （清空选择）；setTabPlacement(99)/setTabLayoutPolicy(99)/setDisplayedMnemonicIndexAt(0,99)
  →IllegalArgumentException。
- 易错：getTabRunCount 依赖 UI 布局（未显示 pane 上只有空 pane 的 0 可靠，加了 tab 后
  不断言）；JTabbedPane.getUI()（返回 TabbedPaneUI）在 JDK-8 就存在（与 JDK-27 新增的
  JComponent.getUI() 不同），两侧可同断言 MetalTabbedPaneUI。
- 环境：headless 下 Metal LaF 先装（UIManager.setLookAndFeel(new MetalLookAndFeel())），
  所有组件操作 EDT（SwingUtilities.invokeAndWait）；NPE/Headless 只断类型。

自动化实现位于同目录 `JTabbedPaneTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印 `PASS (N assertions)`；
jtreg `@run main` 直接驱动。
