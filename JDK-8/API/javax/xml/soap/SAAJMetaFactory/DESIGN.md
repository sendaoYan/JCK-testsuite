# javax.xml.soap.SAAJMetaFactory 契约测试设计

## 1. API 功能

`SAAJMetaFactory` 是 SAAJ 的**服务提供者引导骨架**（抽象类）：定义两个 protected abstract 工厂钩子 `newMessageFactory(String protocol)` 与 `newSOAPFactory(String protocol)`，由 RI（`com.sun.xml.internal.messaging.saaj.soap.SAAJFactoryImpl` 等）实现；`MessageFactory.newInstance()/SOAPFactory.newInstance()` 的静态查找链最终落到这里。同时是第三方替换 SAAJ 实现的扩展点（系统属性 `javax.xml.soap.MetaFactory` 指向自定义子类）。

## 2. 使用方式

标准代码从不直接使用：`MessageFactory.newInstance()` 内部经包私有静态 `getInstance()` + `DEFAULT_META_FACTORY_CLASS` 定位实现类并反射实例化，再调 `newMessageFactory(protocol)`。测试通过匿名/命名子类实例化并桥接到平台查找链，验证钩子协议语义（1.1/1.2 协议 → 对应 envelope 命名空间）。

## 3. 参考来源

- Oracle javadoc Java SE 8：docs.oracle.com/javase/8/docs/api/javax/xml/soap/SAAJMetaFactory.html
- JSR 67 (SAAJ 1.3) 附录（SPI 查找顺序）
- OpenJDK 8u 二进制（javap -p 核对）：protected 构造、两个 protected abstract 钩子、包私有 static getInstance()、私有常量 META_FACTORY_CLASS_PROPERTY/DEFAULT_META_FACTORY_CLASS
- 本仓库测试：`JDK-8/API/javax/xml/soap/SAAJMetaFactory/SAAJMetaFactoryTest.java`

## 4. 测试用例设计 + 易错规范细节

维度：SPI 骨架反射（无公开构造/唯一 protected 构造/两钩子签名与修饰符/getInstance 隐藏入口）→ 匿名子类实例化 → 命名子类桥接（产物非空、协议语义、负例传导）→ 直接实例化拒绝 → SPI 链最终产物 envelope ns。

易错点（实测锁定）：

1. **`getDeclaredMethods()` 返回 3 个方法**：除两个 protected abstract 钩子外还有**包私有 static `getInstance()`**（javap -public 看不见，易漏）。
2. 跨包 `Class.newInstance()` 抛 **IllegalAccessException**（protected 构造对 javax.xml.soap 包外不可达）——不是 InstantiationException。
3. 钩子声明抛 `javax.xml.soap.SOAPException`，参数是协议字符串（取 `SOAPConstants.*_PROTOCOL` 常量）；坏协议经桥接传导为 SOAPException("Unknown Protocol ...")。
4. SPI 链最终产物断言：默认查找 → 1.1 envelope ns `http://schemas.xmlsoap.org/soap/envelope/`；`newInstance(SOAP_1_2_PROTOCOL)` → `http://www.w3.org/2003/05/soap-envelope`。
5. 子类化只需实现两个钩子；匿名子类即刻具体化（getSuperclass()==SAAJMetaFactory.class）。
