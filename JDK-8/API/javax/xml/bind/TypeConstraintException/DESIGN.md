# javax.xml.bind.TypeConstraintException 契约覆盖设计（JDK-8）

## 1. API 功能
动态类型约束违规（如 xsi:type 指向不兼容类型）时抛出的**非受检**异常。关键规范事实：**extends RuntimeException，不是 JAXBException**——但 API 面与 JAXBException 惊人地相似（getErrorCode/getLinkedException/setLinkedException/toString 覆写）。JAXB 1.0 时代它走受检风格的历史设计在 2.x 里保持为非受检。

## 2. 使用方式
```java
TypeConstraintException te = new TypeConstraintException("msg", "code");
te.getErrorCode();
te.getLinkedException();
throw te;   // 无需 throws 声明
```

## 3. 参考来源
- Oracle javadoc: https://docs.oracle.com/javase/8/docs/api/javax/xml/bind/TypeConstraintException.html
- JSR 222 (JAXB 2.2) 规范
- OpenJDK 8u RI 源码 `javax/xml/bind/TypeConstraintException.java`
- 本仓库测试：`TypeConstraintExceptionTest.java`

## 4. 测试用例设计 + 易错规范细节
- **核心继承断言（双向 isAssignableFrom 均 false）**：与 JAXBException 无任何继承关系；与 RuntimeException 有。
- 构造矩阵 5 形态全测。
- **cause 语义与 JAXBException 相反（实测锁定）**：`(Throwable)`/`(String,Throwable)`/`(String,String,Throwable)` 形态下 `getLinkedException()==t` 但 `getCause()==null`；`setLinkedException(t2)` 后 cause 仍为 null——**不同步**。
- `(Throwable)` 形态 message 为 null。
- serialVersionUID 声明值 `-3059799699420143848`（显式声明，getDeclaredField + lookup 双通道）。
- toString 带 linked 时含 " - with linked exception:"；未受检通道：无 throws 声明直接 throw 并被 `catch (RuntimeException)` 捕获。
- 易错点：把本类当 JAXBException 子类测试是最大陷阱；cause 断言必须按"永不同步"写。
