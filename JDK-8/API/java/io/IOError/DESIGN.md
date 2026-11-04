# java.io.IOError 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(Throwable)`；继承 Error（未检查）；getMessage 默认为 cause.toString()。

## 2. Java 用户如何使用
- 将严重 I/O 失败作为不可恢复 Error 抛出；包装任意 Throwable。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/IOError.java`。

## 4. 测试用例设计 + 自动化实现（IOErrorTest.java，纯黑盒）
黑盒场景:
1. cause 保留、消息=cause.toString() 且含细节。
2. 包装任意 Throwable（RuntimeException）。
3. 未检查传播（无需 throws）。
4. 是 Error 而非 Exception；作为 Throwable 捕获。
5. toString 命名类 + 栈帧捕获。
- 健壮性: 无反射白盒。
