# javax.swing.ProgressMonitor 设计案

## 1. API 功能
`ProgressMonitor` 显示进度对话框。方法分组：
- 进度/范围：`setProgress/getMinimum/setMinimum/getMaximum/setMaximum`。
- 弹出控制：`setMillisToDecideToPopup/getMillisToDecideToPopup/setMillisToPopup/getMillisToPopup`。
- 状态：`isCanceled/getNote/setNote/close`。

## 2. Java 用户如何使用
用户报告长时间任务的进度：
```java
ProgressMonitor pm = new ProgressMonitor(parent, "working", "done", 0, 100);
pm.setProgress(50);
if (pm.isCanceled()) return;
```
典型链路：构造 → `setProgress` → `isCanceled` → `close`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/ProgressMonitor.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ProgressMonitor.java`）。
- `ProgressMonitorInputStream` 内嵌使用 `ProgressMonitor` 报告读取进度。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验实现 `Accessible`。
- 行为（黑盒）：`testBlackBox()` 覆盖范围 get/set、`setProgress`、`isCanceled`、`setNote/getNote`、弹出阈值 get/set、`close`（headless 下不真正弹窗）。
