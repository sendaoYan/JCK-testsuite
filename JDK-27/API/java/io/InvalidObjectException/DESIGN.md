# java.io.InvalidObjectException 测试方案 (DESIGN)

## 1. API 功能
- `(String)` / `(String, Throwable)`（JDK 19+）：反序列化出的对象未通过自检（validateObject 抛出）或 readUnshared 遇到回引用。

## 2. Java 用户如何使用
- 在 readObject 中 `registerValidation` 注册校验回调，回调抛本异常令 readObject 失败；不变量破损即拒绝该对象图。

## 3. 开源代码实际用法调研
- 校验回调抛出传播 — `jdk25u-dev/src/java.base/share/classes/java/io/ObjectInputStream.java`（doCallbacks/validateObject 路径）。
- readUnshared 回引用失败 — ObjectInputStream.readObject0 的 TC_ENDBLOCKDATA/unshared 分支（官方 `test/jdk/java/io/Serializable/readUnshared/` 系列覆盖）。

## 4. 测试用例设计 + 自动化实现（InvalidObjectExceptionTest.java）
- 白盒: extends ObjectStreamException、2 构造、受检。
- 黑盒场景:
  1. 两构造的 message/cause。
  2. 真实触发 1：registerValidation 回调抛 InvalidObjectException("invariant broken") 从 readObject 传播且 message 一致。
  3. 真实触发 2：同一对象写两次后第二个回引用用 readUnshared 读 → 抛出。
  4. 可按 IOException 捕获。
- 健壮性: 全内存流。

产出：`InvalidObjectExceptionTest.java`（白盒 4 + 黑盒 6 断言）。
