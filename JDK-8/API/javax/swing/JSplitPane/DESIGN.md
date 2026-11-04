# javax.swing.JSplitPane 测试设计

## 1. API 功能
双区可调分割容器（JComponent + Accessible）：HORIZONTAL_SPLIT=1/VERTICAL_SPLIT=0
两种方向；left/right 与 top/bottom 是同一对槽位的别名（VERTICAL 语义）；
divider（分隔条尺寸/位置 int 或比例 double）、resizeWeight（空间再分配权重
[0,1]）、oneTouchExpandable/continuousLayout 开关；默认构造器自动装两个
JButton 占位。

## 2. Java 用户如何使用
`JSplitPane sp = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, tree, detail);`
主从布局（文件管理器/邮件客户端）；`sp.setOneTouchExpandable(true)` 加收起按钮；
`sp.setResizeWeight(0.5)` 平分多余空间。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`src/java.desktop/share/classes/javax/swing/JSplitPane.java`；
`test/jdk/javax/swing/JSplitPane/`（414 browsertest 等）。

## 4. 测试用例设计 + 易错细节
- 常量：HORIZONTAL_SPLIT=1/VERTICAL_SPLIT=0（与多数 Swing 方向常量 0=水平不同，
  易混）。
- 默认契约：水平方向、**默认构造器装 left/right 两个 JButton 占位**（不是空）、
  continuousLayout false、oneTouch false、resizeWeight 0.0、dividerSize 10、
  getUIClassID=="SplitPaneUI"、Metal→MetalSplitPaneUI、
  getMinimumDividerLocation()>=0（具体值随布局，不锁）。
- 构造器矩阵：(orient)/(orient,bool)/(orient,l,r)/(orient,bool,l,r) 全语义；
  new JSplitPane(999)→IllegalArgumentException。
- **槽位别名**：setTopComponent 更新 getLeftComponent（同一引用），bottom↔right；
  setLeft/RightComponent(null) 合法清空。
- **lastDividerLocation 序列（状态机）**：默认 0 → 第一次 setDividerLocation 后
  变 **-1** → 第二次后是前一次位置（0→-1→40）；setLastDividerLocation 直接往返。
- divider 位置：int 往返（未显示时字段直存）；setDividerLocation(0.5) 合法、
  (-5) 不抛；**越界比例 1.5/-0.1→IllegalArgumentException**。
- resizeWeight：**1.5/-0.1→IAE（"weight must be between 0 and 1" 断类型）**；
  边界 0.0/1.0 合法。
- **setDividerSize(-1) 版本差异**：JDK-8 原样存 -1（无校验）；JDK-27 拒负保持
  10。两侧各按各自行为断言；0 两侧都接受。
- 方法名坑：是 isContinuousLayout()（无 getContinuousLayout）。
- 环境：headless 下 Metal LaF 先装；EDT 操作。

自动化实现位于同目录 `JSplitPaneTest.java`：`check(boolean,msg)` 计数断言、
`checkThrows(Class,Runnable)` 校验精确异常类型，`main` 结尾打印
`PASS (N assertions)`；jtreg `@run main` 直接驱动。
