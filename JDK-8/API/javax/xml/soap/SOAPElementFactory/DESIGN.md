# javax.xml.soap.SOAPElementFactory 契约测试设计

## 1. API 功能

`SOAPElementFactory` 是 SAAJ 1.2 时代的 XML 片段工厂，**自 SAAJ 1.3 起弃用**，替代品是 `SOAPFactory.createElement(...)` 家族。它只能创建独立 `SOAPElement` 片段（最终要靠 `SOAPElement.addChildElement(SOAPElement)` 拷贝挂入真实消息树），不能像 SOAPFactory 那样创建 Name/Detail/Fault。具体类，私有构造 + 静态 `newInstance()`。

## 2. 使用方式

`SOAPElementFactory.newInstance()` → `create(Name)` / `create(String localName)` / `create(String localName, String prefix, String uri)` →（可选）`body.addChildElement(fragment)` 拷贝导入。内部实现是对 `SOAPFactory` 对应方法的纯委托（JDK-8 rt.jar 中持有私有 `SOAPFactory` 字段）。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SOAPElementFactory.html
- JSR 67 (SAAJ 1.3) 弃用说明
- OpenJDK 8u 源码 `javax/xml/soap/SOAPElementFactory.java`
- 本仓库测试：`JDK-8/API/javax/xml/soap/SOAPElementFactory/SOAPElementFactoryTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：反射面（具体类/私有构造/恰好 3 个 create 重载/静态 newInstance）→ 三 create 形态回读 → 独立性与可移植性 → null 负例 → 与 SOAPFactory 的委托一致性。

易错点（实测锁定）：

1. **JDK-8 的弃用是纯 javadoc 级**：类与 create 方法都只有 `@deprecated` javadoc 标签，**运行期没有 `@Deprecated` 注解**（`getAnnotation(Deprecated.class)==null`、`getDeclaredAnnotations()` 长度 0）——但 `javac -Xlint:deprecation` 仍会告警。断言以反射事实为准。
2. `create(String,String,String)` 参数序为 **(localName, prefix, uri)**——与 `SOAPFactory.createElement(String,String,String)` 一致；第 2 参落前缀、第 3 参落 URI（javadoc 文字容易记反）。
3. **第 2 参传非法 NCName（如 `urn:other`）会抛未检查的 `org.w3c.dom.DOMException(NAMESPACE_ERR)`**——RI 把参数直通 DOM 创建，不包装成 SOAPException；负例要按 DOMException 断言。
4. `create((String)null)`/`create((Name)null)` 抛 SOAPException；`create(null,"p","u")` 消息含 `Null localName`（注意与 1 参形态的 `Null tagName` 消息不同）。
5. 无公开构造（`getConstructors().length==0`），实例只能经 `newInstance()`。
6. 产物独立无父；`body.addChildElement(fragment)` 拷贝导入后原实例与挂入实例不是同一对象。
