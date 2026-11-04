# javax.crypto.ExemptionMechanismSpi 设计案

## 1. API 功能
豁免机制 SPI 抽象基类（public abstract，public 无参构造器），6 个 protected abstract 引擎方法（javap 实测）：
- `engineGetOutputSize(int)`
- `engineInit(Key)` / `engineInit(Key, AlgorithmParameterSpec)` / `engineInit(Key, AlgorithmParameters)`（throws InvalidKeyException/InvalidAlgorithmParameterException/ExemptionMechanismException）
- `engineGenExemptionBlob()`（byte[]，ExemptionMechanismException）
- `engineGenExemptionBlob(byte[], int)`（写入并返回长度，ShortBufferException）

## 2. Java 用户如何使用
提供者作者实现全部 6 个方法，注册为 `Provider` 的 `"ExemptionMechanism.<name>"` 服务，终端用户经 `ExemptionMechanism.getInstance(name)` 获得 façade。测试直接实现最小确定性子类：engineGetOutputSize 用可预测映射（2n+1），三个 engineInit 分别记录参数，两个 engineGenExemptionBlob 返回固定 blob 并对短缓冲抛 ShortBufferException；再经 public 桥接方法直调 protected engine* 验证契约语义（偏移写入、独立副本、异常类型、init→generate 组合）。

## 3. 开源代码实际用法调研
web 搜索在当前环境不可用，采用 JDK 源码：
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/ExemptionMechanismSpi.java`：全抽象定义（与 javap 一致：engineGetOutputSize/engineInit×3/engineGenExemptionBlob×2）。
- `~/git/jdk25u-dev/src/java.base/share/classes/javax/crypto/ExemptionMechanism.java` L341-353、L412-437：包装类 init/genExemptionBlob 对 engine* 的全部调用点——说明 SPI 子类必须能独立承担容量检查（engineGenExemptionBlob(byte[],int) 抛 ShortBufferException）与状态检查（包装类不替 engine 记录 doPhase 类状态）。
- JDK 自带测试无 ExemptionMechanismSpi 用例（无内置实现），与本项目实测一致。

## 4. 测试用例设计 + 自动化实现
`ExemptionMechanismSpiTest.java`：
- testWhiteBox：public abstract、父类 Object、public 无参构造器、6 个引擎方法精确签名均为 abstract 且 protected、反射 newInstance 抛 InstantiationException（保留旧测试的方法存在性断言并细化为精确签名）。
- testBlackBox：MinSpi 最小实现——engineGetOutputSize(0)=1、(5)=11 且记录输入长度；三个 engineInit 分别记录 key/spec/params 身份；engineGenExemptionBlob() 两次调用返回内容相等但互不相同的副本并各计数一次；engineGenExemptionBlob(buf,1) 偏移写入且区间外字节为 0；长度 2 或偏移 6 的短缓冲 ShortBufferException；failBlob 开关触发 ExemptionMechanismException；init→generate 组合复跑成功。
