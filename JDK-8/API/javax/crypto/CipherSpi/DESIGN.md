# javax.crypto.CipherSpi 黑盒行为测试设计

## 1. API 功能
- SPI 抽象类（provider 后端）：`public` 构造器；**12 个 protected abstract** 方法：engineSetMode/engineSetPadding/engineGetBlockSize/engineGetOutputSize/engineGetIV/engineGetParameters/engineInit x3/engineUpdate(byte[],int,int)/engineUpdate x5 参/engineDoFinal(byte[],int,int)/engineDoFinal x5 参。
- **7 个 protected 具体（默认实现）** 方法：engineUpdate(ByteBuffer,ByteBuffer)、engineDoFinal(ByteBuffer,ByteBuffer)（均经 bufferCrypt 通用实现）、engineWrap/engineUnwrap/engineGetKeySize/engineUpdateAAD x2（默认抛 UnsupportedOperationException）。

## 2. Java 用户如何使用
密码学 provider 作者继承 CipherSpi 并实现全部抽象方法；`Cipher.getInstance` 经 Provider.Service 反射实例化 SPI 并把 `Cipher.init/update/doFinal` 委派给 engineXxx。独立使用时通常在测试里做最小实现以验证 SPI 契约：
```java
class MiniSpi extends CipherSpi {
    protected int engineGetBlockSize() { return 7; }
    protected byte[] engineUpdate(byte[] in, int off, int len) { ... }
    // ... 其余抽象方法
}
```
默认 engineUpdate(ByteBuffer,ByteBuffer) 会按需借 engineGetOutputSize 判容量、分段经 engineUpdate(byte[]…) 搬运。

## 3. 开源代码实际用法调研
- `~/git/jdk25u-dev/src/java.base/share/classes/com/sun/crypto/provider/AESCipher.java` 等 SunJCE 实现：全部继承 CipherSpi 并覆写抽象 engineXxx（实现侧权威样例）。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/NullCipherSpi.java` L41–112：最小合法实现（engineUpdate 返回拷贝、engineGetBlockSize 返回 1），证明"最小子类即可实例化"。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/CipherSpi.java` L548–556/L720–724（ByteBuffer 默认实现委托 bufferCrypt）、L737+ bufferCrypt（null → NPE、容量不足 → ShortBufferException）、L865–925（engineWrap/engineUnwrap/engineGetKeySize 默认 UnsupportedOperationException）、L952–989（engineUpdateAAD 默认 UnsupportedOperationException）。
- BouncyCastle 低层 JCE（web 调研常识）同样以 CipherSpi 子类注册 provider；此处以 JDK 源码与 javap 为准。

## 4. 测试用例设计 + 自动化实现
白盒场景（`CipherSpiTest.testWhiteBox`，保留原有 10 条方法名断言并补齐）：public abstract、直接父类 Object、public 构造器；12 个抽象 engine 方法逐一按 名字+参数个数+protected+abstract 断言（以 JDK-27 javap 实测：engineSetMode/1、engineSetPadding/1、engineGetBlockSize/0、engineGetOutputSize/1、engineGetIV/0、engineGetParameters/0、engineInit/3、engineInit/4 x2、engineUpdate/3、engineUpdate/5、engineDoFinal/3、engineDoFinal/5）；7 个默认具体方法按 名字+参数个数+protected+非 abstract 断言。
黑盒场景（`testBlackBox`，经静态嵌套 MiniSpi.exercise() 从子类体内调用受保护方法）：
- 抽象方法分派：engineGetBlockSize→7、engineGetOutputSize(11)→12、engineGetIV→[1,2,3,4]、engineGetParameters→null。
- update/doFinal 拷贝语义：返回给定区间、按 outOff 落位、返回长度正确（3 参/5 参四种形态）。
- engineSetMode("XYZ")→NoSuchAlgorithmException、engineSetPadding("BadPad")→NoSuchPaddingException（子类主动抛出）。
- engineInit 3 个重载可调用（null 参数安全）。
- 默认 ByteBuffer 实现（bufferCrypt）：engineUpdate(BB,BB) 拷贝 3 字节、双双推进 position；engineDoFinal(BB,BB) 同理；空输入 update 返回 0；(null,null)→NullPointerException；2B 输出对 5B 输入→ShortBufferException（容量经 engineGetOutputSize 计算）。
- 未覆写的默认方法：engineWrap/engineUnwrap/engineGetKeySize/engineUpdateAAD(byte[]…)/engineUpdateAAD(ByteBuffer) 全部抛 UnsupportedOperationException。
注：任务书提到的 "engineUpdateAndGetRemaining" 在 JDK-27 javap 中不存在，按实际方法面断言。
