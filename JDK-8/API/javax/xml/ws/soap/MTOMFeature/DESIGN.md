# javax.xml.ws.soap.MTOMFeature — 设计文档

## 一、API 功能
`MTOMFeature`（final，扩展 WebServiceFeature）是 MTOM 特性的编程形式：4 个构造器——()、(boolean enabled)、(int threshold)、(boolean, int)——getter getThreshold()（继承 isEnabled()）；常量 `ID = "http://www.w3.org/2004/08/soap/features/http-optimization"`。threshold < 0 时构造即抛 WebServiceException。

## 二、使用方式
`new MTOMFeature(4096)` 传入 features 变参启用带阈值的 MTOM；或由运行时从 @MTOM 实例化。测试覆盖 4 构造器矩阵（含 0 与 Integer.MAX_VALUE 边界）、负阈值负例（精确消息）、ID 常量、实例独立性、与 @MTOM 默认值互通。

## 三、参考来源
- Oracle javadoc: javax.xml.ws.soap.MTOMFeature
- JSR 224 §9.6.1；SOAP MTOM 1.0
- OpenJDK 8 内置 RI
- 本仓库测试：JDK-8/API/javax/xml/ws/soap/MTOMFeature/MTOMFeatureTest.java

## 四、测试用例设计 + 易错规范细节
- 构造矩阵：() → (true,0)；(b) → (b,0)；(t) → (true,t)；(b,t) 全指定。无 (int,boolean) 反序形态（编译期即无该重载）。
- 负例：threshold=-1/-2/MIN_VALUE → WebServiceException，消息含 "must be >= 0" 且带实际值（"MTOMFeature.threshold must be >= 0, actual value: -1"）。
- 合法边界：0 与 Integer.MAX_VALUE 均接受并精确回读。
- ID 与 AddressingFeature.ID 互异；getID() 跨构造器恒等。
- 易错点：先跑负例再建合法实例不受影响；equals 为身份语义（feature 不可变，比较应逐字段）。
