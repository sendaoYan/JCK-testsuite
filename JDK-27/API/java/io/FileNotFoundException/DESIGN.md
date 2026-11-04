# java.io.FileNotFoundException 测试方案 (DESIGN)

## 1. API 功能
- `(String reason)`：失败文件系统操作的信号异常（FileInputStream/FileOutputStream/RandomAccessFile 打不开时抛出），message 通常含失败路径与 errno 描述。

## 2. Java 用户如何使用
- 打开不存在/无权限/是目录的路径时捕获，向用户报告路径问题或回退默认位置。

## 3. 开源代码实际用法调研
- FileInputStream/FileOutputStream/RandomAccessFile 构造抛出 — `jdk25u-dev/src/java.base/share/classes/java/io/FileInputStream.java`（open 失败包装为 FNFE）。
- 官方测试覆盖缺失文件/目录路径 — `jdk25u-dev/test/jdk/java/io/FileInputStream/` 与 `FileOutputStream/`。

## 4. 测试用例设计 + 自动化实现（FileNotFoundExceptionTest.java）
- 白盒: extends IOException、两构造。
- 黑盒场景:
  1. 构造器 message。
  2. 读不存在的路径：FIS 抛出且 message 含该路径；RAF("r") 同样抛出。
  3. 打开目录路径读取抛出。
  4. 在不存在的父目录下创建输出文件抛出。
  5. 对照：临时文件可正常打开写入。
  6. 可按 IOException 捕获且 toString 带类名。
- 健壮性: 全部路径用 tmpdir+纳秒唯一名，不依赖权限场景，不产生残留文件。

产出：`FileNotFoundExceptionTest.java`（白盒 3 + 黑盒 9 断言）。
