# javax.xml.ws.soap.AddressingFeature — 设计文档

## 一、API 功能
`AddressingFeature`（final，扩展 WebServiceFeature）是 WS-Addressing 特性的编程形式：4 个构造器——()（enabled+ALL）、(boolean)、(boolean,boolean)（enabled,required）、(boolean,boolean,Responses)——与 getter isEnabled()/isRequired()/getResponses()；常量 `ID = "http://www.w3.org/2005/08/addressing/module"`。

## 二、使用方式
编程式创建后传给 Service.getPort/createDispatch/Endpoint 的 features 变参，覆盖注解声明；或由运行时从 @Addressing 注解实例化。测试覆盖 4 构造器全矩阵、ID 常量、实例独立性。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.AddressingFeature
- JSR 224 §9.6.1；WS-Addressing 2005/08
- OpenJDK 8 内置 RI
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/AddressingFeature/AddressingFeatureTest.java

## 四、测试用例设计 + 易错规范细节
- 构造矩阵：() → (true,false,ALL)；(b) → (b,false,ALL)；(b,r) → (b,r,ALL)；(b,r,resp) 全指定。参数顺序固定，无 (Responses) 单参形态（实测无 (int,boolean) 类混淆形态）。
- getResponses() 返回枚举；getID() 恒等 ID 常量，与所有构造器无关。
- 类面：final（不可继承）、public；WebServiceFeature 基类 isEnabled/getID 继承可见。
- 实例独立：两实例 equals 为身份语义（未覆写）；各字段互不影响。
- 易错点：required 默认 false、enabled 默认 true 与 @Addressing 一致——两侧断言要交叉引用同一事实。
