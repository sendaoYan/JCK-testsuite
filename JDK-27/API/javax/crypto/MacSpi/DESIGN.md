# javax.crypto.MacSpi 设计案

## 1. API 功能

MAC 服务提供者接口（SPI），`public abstract class`，供 Provider 实现者继承：

- **构造器**：public 无参构造器（Provider 实现者直接子类化）。
- **必须实现的抽象方法**（protected）：`engineGetMacLength()`、`engineInit(Key, AlgorithmParameterSpec)`、`engineUpdate(byte)`、`engineUpdate(byte[], int, int)`、`engineDoFinal()`、`engineReset()`。
- **可覆写的默认实现**：`engineUpdate(ByteBuffer)`——默认把缓冲内容按块转交 `engineUpdate(byte[], int, int)`。
- **公共方法**：`clone()`——`instanceof Cloneable` 时 `super.clone()`，否则抛 `CloneNotSupportedException`。

## 2. Java 用户如何使用

普通用户不直接用它：`Mac.getInstance(...)` 背后由 Provider 提供实现。两类专业用法：
(1) **自定义 Provider**：继承 MacSpi 实现全部抽象方法，经 `Provider.put("Mac.Foo", ...)` 注册；或用 `Mac` 的 protected 构造器 `Mac(spi, provider, "X")` 直接组装一个引擎（本测试即用此路径验证分派）。
(2) **测试驱动**：在子类里写 public 适配方法调用 `engineInit/engineUpdate/...`，从包外驱动受保护方法。

## 3. 开源代码实际用法调研

（WebSearch 无直接命中，以下为 JDK 源码与自带测试，标注路径。）

- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/MacSpi.java` L109-131：`engineUpdate(ByteBuffer)` 默认实现按 1KB 块读出后调 `engineUpdate(b, ofs+pos, lim-pos)`（有 backing array 时一次转交）——对应黑盒"ByteBuffer 委派"断言（heap buffer 恰好触发 1 次 engineUpdate(byte[],int,int)，direct buffer 走循环路径）。
- 同文件 L154-158：`clone()` 源码 `if (this instanceof Cloneable) return super.clone(); else throw new CloneNotSupportedException();`——对应黑盒克隆两断言。
- `~/git/jdk25u-dev/src/java.base/share/classes/com/sun/crypto/provider/HmacCore.java`：SunJCE 的真实 MacSpi 子类（`engineInit` 换 key、`engineDoFinal` 输出并 `engineReset`），是"最小子类"设计的原型。
- `~/git/jdk25u-dev/test/jdk/javax/crypto/Mac/NullMacSpi.java`：JDK 自带的最小 MacSpi 桩（用于验证 Mac 包装不触碰数据），与本测试 `FixedMacSpi`/`NonCloneableSpi` 同思路。
- `~/git/jdk25u-dev/test/jdk/com/sun/crypto/provider/Mac/DigestCloneabilityTest.java`：逐 Provider 验证 SPI `Cloneable` 行为。

## 4. 测试用例设计 + 自动化实现

`MacSpiTest.java`，`testWhiteBox()`（结构，16 check）：public abstract、extends Object、无接口、public 无参构造器、6 个抽象方法均为 protected abstract、`engineUpdate(ByteBuffer)` 为 protected 具体方法、`clone()` public 具体、`engineInit(Key, AlgorithmParameterSpec)` / `byte[] engineDoFinal()` / `int engineGetMacLength()` 精确签名、update 重载签名。

`testBlackBox()`（行为，19 check，SPI 类按 ≥5 要求）场景（`FixedMacSpi` 最小子类返回固定 MAC 长度 7 并计数）：

1. `engineGetMacLength` 返回子类固定值 7。
2. `engineInit` 收到传入 Key 与 spec（引用级断言）。
3. `engineUpdate(byte)` 收到字节；`engineDoFinal` 返回定长 tag 且 tag 内容反映 update 计数（可观察状态）。
4. `engineReset` 清零内部状态（下一次 doFinal 的 tag 反映重置）。
5. 默认 `engineUpdate(ByteBuffer)`：heap buffer 委派恰 1 次 `engineUpdate(byte[],int,int)` 且整缓冲消费；direct buffer 同路径。
6. `clone()`：Cloneable 子类返回同类副本且携带状态；非 Cloneable 子类抛 `CloneNotSupportedException`。
7. 经 protected `Mac(MacSpi, Provider, String)` 组装包装引擎：init/update×2/doFinal 分派计数、getMacLength/doFinal 透传、reset 到达 engineReset（doFinal 内含 1 次 + 显式 1 次）。
8. 两个 SPI 实例状态互不串扰。
