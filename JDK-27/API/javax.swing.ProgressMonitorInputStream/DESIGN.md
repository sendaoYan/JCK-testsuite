# javax.swing.ProgressMonitorInputStream 设计案

## 1. API 功能
`ProgressMonitorInputStream` 在读取流时显示进度。方法分组：
- 进度：`getProgressMonitor`。
- 流读取：`read/read(byte[])/read(byte[],int,int)/skip/reset/close`。

## 2. Java 用户如何使用
用户包装输入流以显示读取进度：
```java
InputStream in = new ProgressMonitorInputStream(parent, "loading", fileStream);
ProgressMonitor pm = ((ProgressMonitorInputStream) in).getProgressMonitor();
while (in.read() != -1) { ... }
```
典型链路：构造 → `getProgressMonitor` → `read`。

## 3. 开源代码实际用法调研
- JDK 源码 `javax/swing/ProgressMonitorInputStream.java` 是 JDK 自身实现（来源：`~/git/jdk25u-dev/src/java.desktop/share/classes/javax/swing/ProgressMonitorInputStream.java`）。

## 4. 测试用例设计 + 自动化实现
- 结构（白盒）：`testWhiteBox()` 校验父类 `FilterInputStream`。
- 行为（黑盒）：`testBlackBox()` 用内存流覆盖 `getProgressMonitor`、逐字节 `read` 到 EOF、`close`（headless 下不弹窗）。
