# java.io.ObjectInputValidation 测试方案 (DESIGN)

## 1. API 功能
- `validateObject() throws InvalidObjectException`：反序列化完成后由 ObjectInputStream 回调（registerValidation 注册，priority 大者先）。

## 2. Java 用户如何使用
- 在 readObject 中注册校验回调，对象图完整后执行不变量检查，抛 InvalidObjectException 拒绝。

## 3. 开源代码实际用法调研
- 回调调度 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（doCallbacks 按 priority 降序）。
- 官方测试 — `jdk25u-dev/test/jdk/java/io/Serializable/validate/`（优先级顺序与拒绝传播）。

## 4. 测试用例设计 + 自动化实现（ObjectInputValidationTest.java）
- 白盒: 接口、validateObject 抽象、声明 InvalidObjectException、registerValidation 参数类型。
- 黑盒场景:
  1. 回调在对象读完后运行且字段已就位。
  2. 多回调按 10>1>0 优先级执行。
  3. 回调抛 InvalidObjectException 从 readObject 传播（v=500 拒 / v=5 收）。
  4. readObject 之外注册 → NotActiveException。
- 健壮性: 全内存流。

产出：`ObjectInputValidationTest.java`（白盒 5 + 黑盒 4 断言）。
