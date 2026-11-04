# java.beans.IntrospectionException — 设计案

## 一、API 功能
内省失败异常：`extends Exception`（受检异常），JDK-8 与 JDK-27 都**只有**构造器
`IntrospectionException(String mess)`——不存在 `(String, Throwable)` 重载（javap/反射双侧实证）。
`Serializable`，`serialVersionUID = -3728150539969542619L`（双侧实测一致）。

## 二、使用方式
`Introspector.getBeanInfo` 族在失败时抛出，最典型触发：stopClass 不是 bean 类的超类。

## 三、参考来源
- Oracle javadoc：Java SE 8 API Specification（`java/beans/IntrospectionException.html`）；
  Java SE 27 同名页面（一致：均仅 (String) 构造器）。
- JavaBeans Specification 1.01-A（Introspection 章节）。
- OpenJDK `Introspector` 源码中 stopClass 检查（"not superclass of"）与回归思路（仅借鉴思路，代码自写）。

## 四、测试用例设计 + 易错规范细节
| 方法 | 覆盖维度 |
|---|---|
| testLegacy | 保留原断言（Throwable/Exception 层次、toString 非空、三 getter 可调） |
| testInventory | 恰好 1 个公开构造器 (String)；`getConstructor(String,Throwable)` 抛 NoSuchMethodException；无自身公开方法；受检（非 RuntimeException）；Serializable + serialVersionUID 精确值 |
| testMessageContract | message 精确保留；null→null；空串保留；getLocalizedMessage==getMessage；toString 精确格式 `java.beans.IntrospectionException: msg`；fillInStackTrace 返回 this；printStackTrace 输出含类名 |
| testCause | 默认 cause null；initCause 返回 this 且身份存储；二次 initCause → IllegalStateException；initCause(null) 合法 |
| testSerialization | round-trip：message/精确类/默认 cause 保留；initCause 设置的 cause 也保留 |
| testRealTriggers | **真实触发**：stopClass 非超类（String,Integer）/stopClass 为子类（Number,Integer）/stopClass==bean（String,String）→ 精确 IntrospectionException（消息含 "superclass"）；**非触发**：真超类（String,Object）成功、primitive/数组/接口 bean 均不抛 |

### 易错规范细节
- JDK-8 与 JDK-27 的构造器集合相同（仅 (String)），异常链只能通过 `initCause` 补挂。
- stopClass 与 bean 类**相同**也抛 IntrospectionException（不只"非超类"一种）；
  消息形如 `java.lang.Integer not superclass of java.lang.String`（双侧一致）。
- `getBeanInfo(int.class)`、`getBeanInfo(String[].class)`、`getBeanInfo(Runnable.class)`
  均正常返回 BeanInfo，不是该异常的触发条件。
