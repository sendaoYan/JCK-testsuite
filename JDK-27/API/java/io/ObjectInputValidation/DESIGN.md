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

## 深化追加（契约级）

新增维度（`testInterfaceStructure`…`testEqualPriorityLifo`，断言 10 → 35）：
- **优先级矩阵**：`[10,5,1,0,-7]` 回调顺序严格**降序**（负优先级合法且最后执行）。
- **同优先级 LIFO**：同级 a/b/c 注册序 → 回调序 `[c,b,a]`（栈语义，实测锁定）。
- **回调时点图完整性**：回调触发时整张对象图（含嵌套对象字段）已填充完毕。
- **null 回调**：registerValidation(null, prio) → InvalidClassException 消息恰 `null callback`（非 NPE，先实测后写入）。
- **异常传播**：回调抛 ISE（运行时）→ 原样传出；抛 IOException → 包装为 InvalidObjectException 且**保留 cause 链**（可用 InvalidObjectException(String,Throwable) 直接构造）；回调抛 InvalidObjectException → 原样传出。
- **作用域**：仅当前 readObject 调用内有效；读之间计数器清零；readObject 之外调用 registerValidation → NotActiveException。

负例矩阵：
| 操作 | 异常 | 消息子串（8/27 一致） |
|---|---|---|
| registerValidation(null, p) | InvalidObjectException | `null callback` |
| 非 readObject 期间注册 | NotActiveException | — |
| 回调抛受检 IOException | InvalidObjectException | cause 为原始异常 |

版本差异：JDK-27 新增 InvalidObjectException(String,Throwable) 构造器；JDK-8 需 initCause 补 cause。
