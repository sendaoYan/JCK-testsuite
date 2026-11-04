# java.io.CharConversionException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 IOException（受检）；表示字符解码失败。

## 2. Java 用户如何使用
- 在 Reader/InputStreamReader 解码失败时以 IOException 捕获。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/CharConversionException.java`。

## 4. 测试用例设计 + 自动化实现（CharConversionExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息/默认 null、无 cause。
2. 作为 IOException 捕获。
3. Reader 签名抛出的解码失败原样传播。
4. 异常自身序列化保留消息。
5. 合法解码路径正常完成（对比）。
- 健壮性: 显式 UTF-8；无反射白盒。
