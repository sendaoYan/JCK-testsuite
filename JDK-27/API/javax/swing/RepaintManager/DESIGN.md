# javax.swing.RepaintManager 测试设计

## 1. API 功能
重绘管理器：脏区登记/addDirtyRegion、双缓冲开关、全局 currentManager 替换、
invalid 组件队列。

## 2. Java 用户如何使用
`RepaintManager.currentManager(c)`；`setDoubleBufferingEnabled(false)` 做离屏基准测试。

## 3. 开源实际用法调研（本地源码+自带测试，web 未使用）
`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/RepaintManager.java`；
JComponent.repaint() 调用路径；JDK 自带 `test/jdk/javax/swing/` 相应模块用例。

## 4. 测试用例设计 + 易错细节
- currentManager(Component)/(JComponent) 一致；setCurrentManager 全局可见并还原。
- **易错**：非显示中 0x0 组件 addDirtyRegion 的脏区被 bounds 交集裁成空——
  getDirtyRegion 返回空矩形、isCompletelyDirty 恒 false、markCompletelyDirty 无效果。
- 负数脏区静默接受；paintDirtyRegions/validateInvalidComponents/addInvalidComponent 无显示时 no-throw。
- 双缓冲默认 true、开关往返。

自动化实现位于同目录 `*Test.java`：`check(boolean, msg)` 计数断言、`checkThrows` 校验精确异常类型，
`main` 结尾打印 `PASS (N assertions)`；jtreg `@run main` 直接驱动（headless 安全，Metal LaF 先行）。
**JDK-8 vs JDK-27**：addDirtyRegion(null,...)/getDirtyRegion(null) 在 JDK-8 抛 NPE，
JDK-27 静默容忍——两侧分别锁定。
