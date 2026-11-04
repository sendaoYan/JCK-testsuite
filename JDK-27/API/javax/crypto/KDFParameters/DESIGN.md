# javax.crypto.KDFParameters 设计案

## 1. API 功能
JDK 24+ 的 KDF 算法配置标记接口（JDK 27 javap：`public interface KDFParameters`，**无** extends、无成员）：
- 不继承 `java.security.spec.AlgorithmParameterSpec`（后者是"每次派生输入"spec 的父接口；本接口是"算法级配置"，传给 `KDF.getInstance(alg, kdfParameters)` 与 `KDFSpi` 构造器）。
- HKDF 实现不接受任何非 null 配置：SunJCE 对非 null KDFParameters 抛 InvalidAlgorithmParameterException（getNext 源码 "The KDFParameters supplied could not be used in combination..."）。

## 2. Java 用户如何使用
绝大多数用户（HKDF 场景）完全不接触本接口——`KDF.getInstance("HKDF-SHA256")` 即可。需要算法级配置的自定义 KDF provider：定义 `class MyParams implements KDFParameters` → `KDF.getInstance("MyKDF", new MyParams(...))` → 配置传入 `KDFSpi` 子类构造器，`kdf.getParameters()` 回读。null 配置总是合法。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KDFParameters.java`：`public interface KDFParameters {}`（空标记接口）。
- `src/java.base/share/classes/javax/crypto/KDF.java` L201-241 getParameters/getInstance(params)：null 直接走无参路径；非 null 经 `Service.newInstance(kdfParameters)` 传给 SPI 构造器，全链失败抛 InvalidAlgorithmParameterException("The KDFParameters supplied could not be used in combination with the supplied algorithm for the selected Provider")（本机 JDK 27.0.1 实测触发）。
- `src/java.base/share/classes/javax/crypto/KDFSpi.java`：唯一构造器 `protected KDFSpi(KDFParameters)`，engineGetParameters 回读。
- `test/jdk/com/sun/crypto/provider/KDF/*`：全部官方测试均未使用非 null KDFParameters（HKDF 无配置概念），仅验证 getParameters()==null——佐证 null 为常态。
- 层级对照：`javax.crypto.spec.HKDFParameterSpec extends AlgorithmParameterSpec`（javap），与 KDFParameters 正交。

## 4. 测试用例设计 + 自动化实现
`KDFParametersTest.java` 白盒（8 check）：保留旧 3 项（public/isInterface/无父类）+ 无声明方法（标记接口）+ 无父接口 + 不继承 AlgorithmParameterSpec + HKDFParameterSpec 属于另一层级。黑盒（12 check）场景：
1. 自定义 MyParameters 可实例化、以接口类型引用、不同配置实例可区分。
2. `KDF.getInstance("HKDF-SHA256", (KDFParameters) null)`：合法、getParameters null、派生 16B 正常、派生后仍 null。
3. 非 null 自定义配置 → InvalidAlgorithmParameterException（类型+消息非空）。
4. 配置流入自定义 KDFSpi：EchoSpi(p2).engineGetParameters()==p2（同一实例往返）、null 配置回读 null、配置后的 SPI 仍可 deriveKey/deriveData。
5. 派生 spec（HKDFParameterSpec）绝非 KDFParameters（正交概念钉死），且无任何 KDFParameters 也能完成 HKDF 派生。
