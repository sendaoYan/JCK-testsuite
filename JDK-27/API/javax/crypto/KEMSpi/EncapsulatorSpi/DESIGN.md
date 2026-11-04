# javax.crypto.KEMSpi.EncapsulatorSpi 设计案

## 1. API 功能
`KEMSpi` 的嵌套 `public interface`（JDK 27），恰好 3 个抽象方法：
- `engineEncapsulate(int from, int to, String algorithm)` → `KEM.Encapsulated`，无 checked 异常。
- `engineSecretSize()` → int。
- `engineEncapsulationSize()` → int。
由 `KEMSpi.engineNewEncapsulator` 返回，`KEM.Encapsulator` 的全部公共方法都单层委派到本接口。

## 2. Java 用户如何使用
KEM provider 作者在 `KEMSpi.engineNewEncapsulator` 里返回本接口实现（通常匿名/内部类），在其中做真正的 KEM 封装运算并按 from/to/algorithm 裁剪结果。用户测试视角：写两个返回不同固定值的实现（32B 与 16B），经 `KEMSpi.EncapsulatorSpi` 接口引用调用，断言多态分派、参数接收、独立状态；再用内置 DHKEM 的 Encapsulator（它就是本 SPI 的包装）验证同一行为。

## 3. 开源代码实际用法调研
web 搜索不可用，采用 JDK 源码与官方测试：
- `src/java.base/share/classes/javax/crypto/KEMSpi.java`（嵌套 interface EncapsulatorSpi）：3 个抽象方法声明，javap JDK 27 核对（engineEncapsulate 无 throws，两个 size 方法 int）。
- `src/java.base/share/classes/javax/crypto/KEM.java` L264-296：`Encapsulator.encapsulate(from,to,alg) → e.engineEncapsulate(from,to,alg)`、`secretSize() → e.engineSecretSize()`、`encapsulationSize() → e.engineEncapsulationSize()` —— 公共 API 是纯委派。
- `src/java.base/share/classes/com/sun/crypto/provider/DHKEM.java`：SunJCE 返回实现本接口的内部类（按 X25519/P-256 分支给出 secretSize 32、encapsulationSize 32/65）。
- `test/jdk/javax/crypto/KEM/RSA_KEM.java`：手写 `KEMSpi.EncapsulatorSpi` 固定实现，`engineEncapsulate` 内 `new KEM.Encapsulated(...)`。

## 4. 测试用例设计 + 自动化实现
`EncapsulatorSpiTest.java` 白盒（11 check）：保留旧 4 项（public/isInterface/无父类/3 方法名）+ 恰好 3 方法 + engineEncapsulate 精确签名（int,int,String→KEM$Encapsulated，abstract，无 checked 异常）+ 两个 size 方法 abstract int + 嵌套于 KEMSpi。黑盒（17 check）场景：
1. impl A（32B 固定值）：经接口引用 secretSize/encapsulationSize==32，engineEncapsulate(0,32,"Generic") 返回固定 secret/ct 字节、params null，from/to/algorithm 被记录。
2. impl B（16B/3B/params{1}）：换绑同一接口变量后尺寸与固定值独立于 A，无共享状态。
3. 重复调用返回内容相等的新 Encapsulated 实例。
4. 内置 DHKEM 封装器履行同契约：32/32 尺寸、encapsulate(0,16,"AES") 产出 16B AES 密钥。
