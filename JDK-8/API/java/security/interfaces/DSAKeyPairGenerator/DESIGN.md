# java.security.interfaces.DSAKeyPairGenerator — 设计案

## 1. API 功能
DSA 专用初始化接口：两个 initialize 重载——(DSAParams, SecureRandom) 与 (int modlen, boolean genParams, SecureRandom)。平台 KeyPairGenerator 并不实现它（历史上 provider SPI 用）。

## 2. 使用方式
由实现 provider 密钥对生成器的代码实现此接口以接收 DSA 专用参数；应用层一般经 KeyPairGenerator.initialize(DSAParameterSpec) 间接到达。

## 3. 参考来源
- javadoc `java.security.interfaces.DSAKeyPairGenerator`（两个 initialize 的 InvalidParameterException 语义）。
- javap 双侧对照。

## 4. 测试用例设计 + 易错规范细节
- 成员面精确断言：恰好 2 个 initialize 重载、均为 void、throws 子句只声明非受查的 InvalidParameterException（JDK-8 声明之；JDK-27 已删除该 throws 子句——版本差异）、接口总方法数恰为 2。
- 参数签名精确断言：(DSAParams, SecureRandom) 与 (int, boolean, SecureRandom)。
- 记录型实现：两重载实参逐项捕获（含 null、负 modlen 原样捕获——校验责任在实现）。
- 继承格：无父接口、非 Key、非 Serializable、无 serialVersionUID；平台 KeyPairGenerator（类与 DSA 实例）均不实现本接口。
- 易错点：InvalidParameterException 是 RuntimeException，不出现在受查异常集，但 JDK-8 接口 throws 子句显式声明，getExceptionTypes 可见。
