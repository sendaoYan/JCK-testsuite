# java.util.stream.Stream.Builder 测试设计案

> 覆盖目标：**完整契约**。headless 安全，临时文件 finally 清理。

## 1. API 功能

消费器→流 两阶段状态机；build 后 add/accept ISE。

## 2. Java 用户如何使用

add 返回 this 链式；accept 无返回；空 builder 空 流。

## 3. 参考来源

javadoc（unified contract）。

## 4. 测试用例设计

BuilderTest.testContracts：链式、混合、build 后 ISE×2、空。
