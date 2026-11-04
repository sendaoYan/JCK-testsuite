# java.beans.IntrospectionException — 设计案

## 1. API 功能
内省异常（extends Exception）。构造器 `(String)`。

## 2. Java 用户如何使用
`Introspector.getBeanInfo` 失败时抛 IntrospectionException。

## 3. 开源代码实际用法调研
JDK 源码 `IntrospectionException.java`。

## 4. 测试用例设计 + 自动化实现
白盒：extends Exception。黑盒（`testBlackBox`）：message 与 null message。

---

## 追加覆盖（契约深化，JDK-8 与 JDK-27 行为一致，均为实测）

### 新增测试方法
| 方法 | 覆盖维度 |
|---|---|
| testInventory | 恰好 1 个公开构造器 (String)；无 (String,Throwable) 重载（NoSuchMethodException）；无自身公开方法；受检；Serializable + serialVersionUID=-3728150539969542619L |
| testMessageContract | message/null/空串；getLocalizedMessage==getMessage；toString 精确格式；fillInStackTrace/printStackTrace |
| testCause | initCause 链：默认 null、身份存储、二次抛 IllegalStateException、initCause(null) 合法 |
| testSerialization | round-trip：message/精确类/cause 保留 |
| testRealTriggers | **真实触发**：getBeanInfo(String,Integer)（stop 非超类）、(Number,Integer)（stop 为子类）、(String,String)（stop==bean）→ 精确 IntrospectionException；非触发：(String,Object) 成功，primitive/数组/接口 bean 不抛 |

### 新增易错细节（实测）
- stopClass 与 bean 类相同也抛（不只"非超类"）；消息形如 `java.lang.Integer not superclass of java.lang.String`（双侧一致）。
- 异常链只能经 initCause 补挂（无 (String,Throwable) 构造器，双侧同）。
