# javax.crypto.KEMSpi.DecapsulatorSpi 设计案

## 1. API 功能
`KEMSpi` 的嵌套 `public interface`（JDK 27），恰好 3 个抽象方法：
- `engineDecapsulate(byte[] encapsulation, int from, int to, String algorithm)` → `SecretKey`，声明抛 `DecapsulateException`。
- `engineSecretSize()` → int。
- `engineEncapsulationSize()` → int。
由 `KEMSpi.engineNewDecapsulator` 返回，`KEM.Decapsulator` 的公共方法单层委派到本接口。

## 2. Java 用户如何使用
KEM provider 作者实现本接口（真正的解封装运算+按 from/to 裁剪+失败抛 DecapsulateException）；用户侧 `KEM.Decapsulator.decapsulate(...)` 即调到这里。测试视角：手写两个固定实现（32B 严格校验长度 / 16B 宽松），经接口引用断言多态分派、参数快照（防御性 clone 语义）、失败异常消息；再以内置 DHKEM 验证真实契约。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KEMSpi.java`（嵌套 interface DecapsulatorSpi）：3 抽象方法；javap JDK 27 核对 engineDecapsulate 声明 DecapsulateException。
- `src/java.base/share/classes/javax/crypto/KEM.java` L391-404：`Decapsulator.decapsulate(...) → d.engineDecapsulate(encapsulation, from, to, algorithm)`，secretSize/encapsulationSize 同样纯委派。
- `src/java.base/share/classes/com/sun/crypto/provider/DHKEM.java`：SunJCE 实现——非法密文（全零、错误长度、点校验失败）在此层抛 DecapsulateException("Cannot decapsulate"/"incorrect encapsulation size")（本机 JDK 27.0.1 实测）。
- `test/jdk/javax/crypto/KEM/RSA_KEM.java`：手写 DecapsulatorSpi 固定实现示例。

## 4. 测试用例设计 + 自动化实现
`DecapsulatorSpiTest.java` 白盒（11 check）：保留旧 4 项 + 恰好 3 方法 + engineDecapsulate 精确签名（byte[],int,int,String→SecretKey，abstract，throws DecapsulateException）+ 两个 size 方法 abstract int + 嵌套于 KEMSpi。黑盒（15 check）场景：
1. impl A（32B）：经接口引用尺寸 32/32，engineDecapsulate 返回固定 secret 且 algorithm 名透传，ct/from/to/algorithm 全部被接收（含 ct 数组快照，事后改调用方数组不影响）。
2. impl A 失败路径：31B 输入抛 DecapsulateException("wrong size: 31")，消息精确（自抛自断），调用计数 2。
3. impl B（16B）：独立尺寸与固定值、独立计数，无共享状态。
4. 内置 DHKEM 解封装器履行同契约：32/32、配对解封一致、全零 32B 密文抛 DecapsulateException。
