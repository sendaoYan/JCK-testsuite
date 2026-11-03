# javax.crypto.KEMSpi 设计案

## 1. API 功能
JDK 21+ 的 KEM 服务提供者接口，`public interface`，两个引擎方法（均抽象）：
- `engineNewEncapsulator(PublicKey, AlgorithmParameterSpec, SecureRandom)` 返回 `KEMSpi.EncapsulatorSpi`，声明抛 InvalidAlgorithmParameterException/InvalidKeyException。
- `engineNewDecapsulator(PrivateKey, AlgorithmParameterSpec)` 返回 `KEMSpi.DecapsulatorSpi`，声明抛同上。
嵌套 `KEMSpi.EncapsulatorSpi` / `KEMSpi.DecapsulatorSpi` 两个子接口。Provider 注册 `KEM.DHKEM` 等服务时实现本接口；`KEM.getInstance` 的延迟 provider 选择即遍历各服务的本接口实现。

## 2. Java 用户如何使用
第三方 crypto provider 作者：实现 `KEMSpi`（两个工厂方法分别返回实现 `EncapsulatorSpi`/`DecapsulatorSpi` 的对象）→ 经 `Provider.Service` 注册 → 用户侧 `KEM.getInstance(...)` 自动路由。测试视角：手写固定返回值的实现类，通过 `KEMSpi` 类型引用分派，断言参数透传、返回值契约与异常传播；再用内置 DHKEM 验证同一契约在真实 provider 上成立。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KEMSpi.java`：接口定义与两个抽象方法签名（javap 核对 JDK 27 一致）。
- `src/java.base/share/classes/javax/crypto/KEM.java` L483-560（DelayedKEM）：`KEMSpi spi = (KEMSpi) service.newInstance(null); return new Encapsulator(spi.engineNewEncapsulator(publicKey, spec, secureRandom), service.getProvider());` —— 引擎方法由框架调用、异常被捕获合流的唯一路径。
- `src/java.base/share/classes/com/sun/crypto/provider/DHKEM.java`：SunJCE 的 KEMSpi 实现（`implements KEMSpi`），engineNewEncapsulator 按 key 算法分支返回 EncapsulatorSpi。
- `test/jdk/javax/crypto/KEM/RSA_KEM.java`：测试内手写完整 KEMSpi 实现（fixed/passthrough 语义），用 `new KEM.Encapsulated(...)` 组装返回值 —— 本测试同构。

## 4. 测试用例设计 + 自动化实现
`KEMSpiTest.java` 白盒（14 check）：保留旧 4 项（public/isInterface/无父类/两方法名）+ 两方法精确签名（参数/返回类型/abstract）+ IAPE/IKE throws 子句 + 两个嵌套 SPI 接口存在且属于 KEMSpi。黑盒（10 check）场景：
1. 手写 FixedKEMSpi：经接口引用调用两个工厂方法返回 EncapsulatorSpi/DecapsulatorSpi 实例，计数各 1 次，公钥/私钥/SecureRandom 实参透传。
2. 产出的 EncapsulatorSpi：engineEncapsulate 返回固定 key/ct 值、尺寸查询为 8，from/to/algorithm 被接收。
3. 产出的 DecapsulatorSpi：正常输入返回固定密钥，空输入抛 DecapsulateException("synthetic decapsulation failure") 且消息原样传播。
4. ThrowingKEMSpi：InvalidKeyException/InvalidAlgorithmParameterException 原样传播。
5. 内置 DHKEM 经同一契约 roundtrip。
