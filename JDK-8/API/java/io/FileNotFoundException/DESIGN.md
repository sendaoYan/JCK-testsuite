# java.io.FileNotFoundException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException；表示文件路径缺失/目录打开/不可达。

## 2. Java 用户如何使用
- 打开 FileInputStream/FileOutputStream/RandomAccessFile 失败时捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/FileNotFoundException.java`。

## 4. 测试用例设计 + 自动化实现（FileNotFoundExceptionTest.java，纯黑盒）
黑盒场景（临时目录隔离）:
1. 两构造器默认 null / 消息保留。
2. 缺失文件 FileInputStream 与 RandomAccessFile("r") 抛异常且消息含路径。
3. 目录当作输入打开抛异常。
4. 父目录不存在的输出路径抛异常。
5. 存在可写文件正常打开（对比）。
6. 作为 IOException 捕获，toString 命名类。
- 健壮性: 无硬编码分隔符；无反射白盒。
