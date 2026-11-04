# java.io.WriteAbortedException 测试方案 (DESIGN)

## 1. API 功能
- 单构造器 `(String, Exception)`；继承 ObjectStreamException；公共字段 `Exception detail`；getMessage 组合 `"msg; detail"`；getCause()==detail。

## 2. Java 用户如何使用
- 写侧中断后，读侧捕获并检查 detail 取回写侧异常。

## 3. 开源代码实际用法调研
- 定义 — `jdk8u-dev/jdk/src/share/classes/java/io/WriteAbortedException.java`。

## 4. 测试用例设计 + 自动化实现（WriteAbortedExceptionTest.java，纯黑盒）
黑盒场景:
1. 构造、detail 字段、组合消息、getCause==detail。
2. 自定义 writeObject 抛异常中止写，读侧包裹原 IOException。
3. 不可序列化成员中止写，读侧包裹 NotSerializableException。
4. 作为 ObjectStreamException 捕获。
- 健壮性: 无反射白盒；serialVersionUID 显式固定。
