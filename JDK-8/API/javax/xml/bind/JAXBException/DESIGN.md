# javax.xml.bind.JAXBException 契约覆盖设计（JDK-8）

## 1. API 功能
JAXB 全部受检异常的基类。携带三个正交信息通道：
- 普通异常通道：`getMessage()`；
- JAXB 特有错误码通道：`getErrorCode()`（`(String,String)` 构造的第二参即 errorCode，默认 null）；
- 老式链式异常通道：`getLinkedException()/setLinkedException(Throwable)`（JAXB 1.0 时代 API，早于 JDK 1.4 的 cause 机制）。

在 JAXB 体系中是 MarshalException/UnmarshalException/PropertyException/ValidationException 的公共父类；`DataBindingException` 与 `TypeConstraintException` 均不继承它。

## 2. 使用方式
```java
JAXBException e = new JAXBException("msg", "ERR-CODE", cause);
e.getErrorCode();            // "ERR-CODE"
e.getLinkedException();      // == cause（RI 中两者同步）
throw e;                     // 受检异常，需 throws 声明
```
典型来源：`JAXBContext.newInstance(未知类名)`、`Marshaller/Unmarshaller.setProperty(未知属性)`、坏 XML unmarshal 等。

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/JAXBException.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `jaxws/src/share/jaxws_classes/javax/xml/bind/JAXBException.java`
- 本仓库测试：`JAXBExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- 构造矩阵 5 形态：`(String)`/`(String,String)`/`(Throwable)`/`(String,Throwable)`/`(String,String,Throwable)`，逐一断言 message/errorCode/linked/cause。
- **(Throwable) 形态 message 为 null**（不是 t.toString()——与 DataBindingException 相反）。
- **RI 关键实现锁定：`getCause()` 被覆写为返回 linkedException**，因此 `getCause()==getLinkedException()` 恒成立；`setLinkedException(t2)` 后两者同步变 t2。
- `initCause` 在 `(Throwable)` 形态上不抛 IllegalStateException（Throwable 层 cause 从未初始化），且不影响 overridden 的 `getCause()`；`(String)` 形态上 `initCause(t)` 成功后 `getLinkedException()/getCause()` 仍为 null（RI 覆写语义，已实测锁定）。
- serialVersionUID 声明值 `-5621384651494307979L`（`getDeclaredField` + `ObjectStreamClass.lookup` 双通道锁定）。
- toString 带 linked 时含 `" - with linked exception:"`；printStackTrace 三个重载不抛且输出含 message 与 linked 文本。
- 空串 errorCode `""` 合法且可回读。
- 易错点：测试断言 cause 时必须区分 JAXBException（同步）与 TypeConstraintException（不同步）两套语义。
