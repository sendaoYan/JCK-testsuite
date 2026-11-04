# java.io.IOException 测试方案 (DESIGN)

## 1. API 功能
- 4 构造器：`()`、`(String)`、`(String, Throwable)`、`(Throwable)`；java.io 异常层次受检根。

## 2. Java 用户如何使用
- 作为大多数 java.io 异常的基类捕获；支持 initCause 与 addSuppressed。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/IOException.java`。

## 4. 测试用例设计 + 自动化实现（IOExceptionTest.java，纯黑盒）
黑盒场景:
1. 四构造器消息/cause 矩阵。
2. initCause 仅一次 + 拒绝自引用。
3. addSuppressed 顺序保留。
4. 异常自身序列化保留消息。
5. 真实 API 抛 EOFException 可作 IOException 捕获。
6. try-with-resources 兼容。
7. toString 格式 + 栈帧捕获。
- 健壮性: 无反射白盒。
