# java.awt.Desktop 行为测试设计

## 1. API 功能
入口：`isDesktopSupported/getDesktop`（单例）；能力查询：`isSupported(Action)`；动作：`open/print/edit/browse(URI)/mail()`；内部枚举 Action（JDK 8 5 个 / JDK 9+ 24 个）。

## 2. Java 用户如何使用
```java
if (Desktop.isDesktopSupported()) {
    Desktop d = Desktop.getDesktop();
    if (d.isSupported(Desktop.Action.OPEN)) d.open(file);
}
```
平台能力自适应：isSupported 先行，不支持的动作走精确异常路径。

## 3. 开源实际用法调研
来源（本地 JDK 源码与自带测试，web 未使用）：
`~/git/jdk25u-dev/src/java.desktop/share/classes/java/awt/Desktop.java`。

## 4. 测试用例设计 + 自动化实现
白盒：public/继承关系/方法面。黑盒：构造→属性往返→事件登记→负例矩阵（`testContractDesktop()`）。
对应 `DesktopTest.java`（本批新增 `testContract*()` 深化方法；断言计数见 PASS 输出）。

## 5. 契约深化（本轮追加）
- X11 实测：isDesktopSupported=true、getDesktop 单例；Linux 仅 OPEN 支持，EDIT/PRINT/MAIL/BROWSE 均 false。
- 不支持动作的异常矩阵（精确类型）：`edit(file)`/`print(file)`/`mail()`/`browse(uri)` 均抛 UnsupportedOperationException（plain，非包装）；`open(不存在文件)` 抛 IllegalArgumentException；`isSupported(null)` 不抛、正常应答 false。
- 版本差异：Action 常量 JDK 8 为 5 个、JDK 9+ 为 24 个（APP_*/BROWSE_FILE_DIR/MOVE_TO_TRASH 等 Linux 上全部 unsupported，按 isSupported=false 断言而非硬编码异常）。
