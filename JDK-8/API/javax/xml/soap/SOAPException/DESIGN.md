# javax.xml.soap.SOAPException 契约测试设计

## 1. API 功能

`SOAPException` 是 SAAJ 全家族唯一的受检异常类型（`extends java.lang.Exception`），携带可选 reason 与链式 cause；在 MessageFactory/SOAPConnection/SOAPFactory/SOAPElement 等所有 SAAJ API 的失败路径上抛出。JDK-8 rt.jar 中的实现**没有** `getDetail/setDetail`（这是与 javadoc 规范文本的重要分叉，见第 4 节）。

## 2. 使用方式

四个公开构造：`()`、`(String reason)`、`(String reason, Throwable cause)`、`(Throwable cause)`。典型捕获：`try { SOAPConnection.call(...) } catch (SOAPException e) { e.getMessage(); e.getCause(); }`。cause 也可事后经 `initCause` 注入（每实例仅一次）。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SOAPException.html
- JSR 67 (SAAJ 1.3)
- OpenJDK 8u 源码 `javax/xml/soap/SOAPException.java`（本仓库测试开发时曾从 JDK src.zip 解出核对 initCause 实现）
- 本仓库测试：`JDK-8/API/javax/xml/soap/SOAPException/SOAPExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：构造矩阵精确回读 → getMessage 动态回退 → initCause 语义（this 返回值/重复 ISE/自引 IAE/null 放行）→ 方法面（JDK-8 二进制事实）→ 嵌套链解包 → 序列化往返 → 抛/捕惯用法。

易错点（实测锁定）：

1. **`(Throwable)` 构造的 message == `cause.toString()`**（`super(cause.toString())`），不是 `cause.getMessage()`，也不是 null。
2. **getMessage 动态回退**：自身 message 为 null 且 cause 非空时返回 `cause.getMessage()`——`() + initCause(e)` 后 `getMessage()` 变成 `e.getMessage()`。
3. `initCause` 重复调用抛 `IllegalStateException`，消息精确为 **"Can't override cause"**；`initCause(this)` 抛 `IllegalArgumentException`；`initCause(null)` 合法且返回 this、cause 保持 null。`(String,Throwable)`/`(Throwable)` 构造已置 cause，再 initCause 一律 ISE。
4. **JDK-8 二进制没有 `getDetail()/setDetail(Detail)`**（SAAJ 规范文本/javadoc 有描述，但 rt.jar 类无此方法，`getMethod("getDetail")` 抛 NoSuchMethodException）——断言以二进制为准。
5. 恰好 4 个公开构造；异常不重写 equals（引用相等）。
6. 序列化往返保留 message 与 cause 的类+消息（Exception 通道，无需显式 serialVersionUID 声明亦可往返，RI 计算默认 SVUID 稳定）。
7. 运行期 stderr 的 com.sun SEVERE 日志行（如 SAAJ0xxx）属正常实现日志，不断言。
