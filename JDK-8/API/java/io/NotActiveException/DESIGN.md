# java.io.NotActiveException 测试方案 (DESIGN)

## 1. API 功能
- `()` 与 `(String)` 构造；继承 ObjectStreamException；表示序列化调用缺少活动上下文。

## 2. Java 用户如何使用
- 仅在 writeObject/readObject 回调内调用 default*/putFields/writeFields/readFields/registerValidation。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/NotActiveException.java`；ObjectStream 类在无上下文时抛出。

## 4. 测试用例设计 + 自动化实现（NotActiveExceptionTest.java，纯黑盒）
黑盒场景:
1. 两构造器消息。
2. 写侧 defaultWriteObject/putFields/writeFields 无上下文抛 NotActiveException。
3. 读侧 defaultReadObject/readFields/registerValidation 无上下文抛 NotActiveException。
4. 回调内部同样调用成功（对比）。
5. 作为 ObjectStreamException 捕获。
- 健壮性: 无反射白盒。
