# java.awt.FileDialog 测试设计

## ① API 功能
文件选择对话框，扩展 Dialog（**构造即 APPLICATION_MODAL**——全部构造器 `super(parent, title, true)`）。
- 常量：`LOAD=0/SAVE=1`；6 个 public 构造器（Frame 族 3 + Dialog 族 3）。
- 模式/路径：`getMode/setMode`、`getDirectory/setDirectory`、`getFile/setFile/getFiles`、`setMultipleMode/isMultipleMode`、`getFilenameFilter/setFilenameFilter`。

## ② Java 用户如何使用
new FileDialog(Frame[, title[, mode]]); get/setMode、get/setFile、get/setDirectory、get/setTitle、setMultipleMode/isMultipleMode、setFilenameFilter；show 后 getFile/getDirectory 读回选择。

## ③ 开源代码实际用法调研
- JDK 源码 `~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/FileDialog.java`：构造器校验 mode（非 LOAD/SAVE 抛 IAE）并 `super(parent, title, true)` 固定 modal；`setMode` 同校验。
- 类 javadoc：X11 平台原生 peer 忽略 FilenameFilter（getter 仍返回最近设置值）。

## ④ 测试用例设计 + 自动化实现
- 白盒：继承 Dialog、6 构造器、声明关键方法、LOAD/SAVE 常量 → `testWhiteBox`。
- 常量精确值 LOAD=0/SAVE=1 → `testBlackBox`。
- 6 构造器矩阵（默认 LOAD、owner 指向、title、SAVE 透传）→ `testBlackBox`。
- null parent（(Frame)null/(Dialog)null）→ getOwner()==null → `testBlackBox`。
- `setMode` SAVE/LOAD 往返 + 失败后模式不变；directory/file 往返 + null 接受/默认 null；`getFiles()` 未显示时空数组；multipleMode 往返；filenameFilter 身份往返（X11 no-op 注释）；`isModal()==true` + `getModalityType()==APPLICATION_MODAL`；toString 含 title → `testBlackBox`。

## ⑤ 反例与精确异常
- `new FileDialog(parent, title, 99)` / `(-1)` → IllegalArgumentException（构造器内校验）。
- `setMode(99)` / `setMode(-1)` → IllegalArgumentException，之后 getMode 不变。

版本差异：JDK-8 与 JDK-27 语义一致（双侧实测相同）。

自动化实现位于同目录 `FileDialogTest.java`：白盒结构断言 `testWhiteBox()`，黑盒组合操作断言 `testBlackBox()`，`check/checkThrows` 计数，`main` 打印 `PASS (N assertions)`；@key headful（DISPLAY=:77），通过 55 断言。
