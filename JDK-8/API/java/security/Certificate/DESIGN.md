# Certificate — 设计案

## 1. API 功能
`java.security.Certificate` 是 JDK 1.1 遗留的**弃用接口**（@Deprecated），由 `java.security.cert.Certificate` 取代。7 个抽象方法：getGuarantor/getPrincipal/getPublicKey/encode(OutputStream)/decode(InputStream)/getFormat/toString(boolean)。encode/decode 声明抛 KeyException+IOException。

## 2. 使用方式
自定义实现承载主体/担保人/公钥三元组；encode 把证书序列化进输出流，decode 反向；toString(false) 简洁 / toString(true) 详细。与 cert 包的 Certificate 无任何继承关系。

## 3. 参考来源
- javadoc `java.security.Certificate`（弃用说明、方法异常契约）。
- javap 双侧（JDK-8 与 JDK-27 方法面完全一致，仍保留于 27）。

## 4. 测试用例设计 + 易错规范细节
- 结构：恰 7 个声明方法（反射计数）、全抽象无 default/static、无父接口、非 Serializable、@Deprecated 注解存在；encode/decode 的 checked 异常子句精确（KeyException+IOException）；toString(boolean) 参数与返回类型。
- 行为：记录型实现断言 getGuarantor/getPrincipal/getPublicKey 身份透传、encode 收到调用方流且字节确定、encode 可重复、decode 收到调用方流身份、toString 两分支由实现定义但互异。
- 异常中继：encode 抛 IOException / decode 抛 KeyException 原样传播（消息逐字）。
- equals：接口无值语义——同内容两个实现实例互不相等，自反恒真。
- 与 java.security.cert.Certificate 的互斥：互不 instanceof、互不 isAssignableFrom。
- 易错点：它是**接口**不是抽象类；与 cert 包 Certificate 同名不同物，断言必须显式区分；Identity.certificates() 默认空（历史消费者）。
