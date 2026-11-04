# javax.swing.ToolTipManager 测试设计

## 1. API 功能
工具提示生命周期管理器（单例，extends MouseAdapter implements MouseMotionListener）：
initial/dismiss/reshow 延迟、轻量弹窗开关、组件注册。

## 2. Java 用户如何使用
`ToolTipManager.sharedInstance().setInitialDelay(500)`；组件 setToolTipText 后自动注册。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ToolTipManager.java`；
JComponent.setToolTipText()；ToolTipManager 注册路径；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- sharedInstance 单例；implements MouseListener+MouseMotionListener（shape 断言）。
- 默认值精确锁定：750/4000/500、enabled、lightweight true；setter 往返并还原默认。
- **易错**：setToolTipText 会自动把共享 manager 挂为监听器；对已注册组件 registerComponent 是
  remove+add 同一实例（计数不变）；unregister 后归零。无 tooltip 组件则 register +1/unregister -1。
- 二次 unregister、未注册组件 unregister 均 no-throw。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。
**JDK-8 vs JDK-27**：JDK-27 侧 ToolTipManager 类为 final（JDK-8 不是），27 侧 shape 断言锁定 isFinal==true。
