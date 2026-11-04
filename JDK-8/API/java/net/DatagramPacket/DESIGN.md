# java.net.DatagramPacket 测试设计案（JDK-8）

> 覆盖目标：完整契约。6 个构造器、get/set 访问器、buffer/offset/length 边界校验（IAE/NPE）、
> 未解析地址拒绝（IAE）、setData(byte[]) 重置 offset、getSocketAddress 往返。全部离线。

## 1. API 功能

- 构造器（6）：`(byte[],int,int)`、`(byte[],int)`、`(byte[],int,int,InetAddress,int)`、
  `(byte[],int,InetAddress,int)`、`(byte[],int,int,SocketAddress)`、`(byte[],int,SocketAddress)`。
- 访问：`getAddress/getPort/getData/getOffset/getLength/getSocketAddress`。
- 修改：`setData(byte[]/byte[],int,int)`、`setAddress`、`setPort`、`setSocketAddress`、`setLength`。

## 2. 用户如何使用

- `new DatagramPacket(buf, len)` 接收；`new DatagramPacket(buf, off, len, addr, port)` 发送。
- `setData` 换缓冲；`setLength` 限制有效载荷。

## 3. 参考来源

- Oracle Java SE 8 javadoc（java/net/DatagramPacket.html）。
- OpenJDK `~/git/jdk8u-dev/jdk/test/java/net/DatagramPacket/`（Constructor、CheckBoundaries、Offset、Unresolved、ReuseBuf）。
- JCK runtime-8b `~/JDK/testsuite/jck8b/JCK-runtime-8b/tests/api/java_net/DatagramPacket/`（CtorTests、set*Tests、SocketAddressTests）。

## 4. 测试用例设计（映射到 DatagramPacketTest.java）

| 优先级 | 测试点 | 方法 |
|---|---|---|
| P0 | 简单构造器 + 默认 offset/地址/端口 | testSimpleConstructors |
| P0 | 带地址构造器（4/6 参 + SocketAddress） | testAddressConstructors |
| P0 | set* 系列 + setData(byte[]) 重置 offset | testSetters |
| P0 | 边界矩阵（负长度/超长/溢出/null/负端口）→IAE/NPE | testBoundaries |
| P0 | 未解析地址/错误 SocketAddress 类型→IAE | testUnresolvedAndBadAddress |

## 易错规范细节（已锁入断言）

- `getData()` 返回**同一**缓冲引用（非拷贝）；`setData(byte[])` 重置 offset=0、length=缓冲长度。
- 长度/offset 越界、`Integer.MAX_VALUE` offset 溢出、负端口、端口>65535 → IllegalArgumentException；null 缓冲 → NPE。
- `setSocketAddress` 仅接受 InetSocketAddress；未解析地址 → IllegalArgumentException。
- JDK-8 特有：无地址包的 `getSocketAddress()` 重建 `InetSocketAddress(null,-1)` → IllegalArgumentException
  （JDK-27 已改为返回 wildcard 0.0.0.0:0，镜像时按各自 JDK 适配）。

---

# 深化批次 G 追加（2026-11-05，~55 → 238 断言级契约覆盖）

## 覆盖缺口分析（深化前已有 5 方法 ~55 断言）

缺：合法边界矩阵（len 0、offset+length==buf.length、offset==buf.length、空缓冲）、
端口边界精确消息表（双侧同措辞 "Port out of range:<v>"）、null 语义全集
（null InetAddress 合法、setAddress(null) 清地址、setSocketAddress(null) 精确 IAE）、
setLength 容量语义（容量随最后一次 setData 更新、offset 参与校验）、setData 重置矩阵、
活缓冲可见性、offset/length 全组合表驱动矩阵、结构反射（6 构造器/全 synchronized）、
回环 send/receive 往返（receive 填充/收缩/保 offset/窗口外字节不动）。

## 新增测试方法

| 方法 | 覆盖 |
|---|---|
| testConstructorBoundaryMatrix | 合法边界 6 格（offset/length/data 三元组断言）+ 非法 7 格（含空缓冲 len1、MIN_VALUE）+ 空缓冲 len0 |
| testPortBoundaryMatrix | 4-arg/6-arg/setPort × {-2,-1,65536,65537,MAX,MIN} 全部精确 "Port out of range:<v>"；0/1/65535 合法往返（含 SocketAddress 构造） |
| testNullSemantics | null 缓冲 4 构造器 NPE；null 地址构造合法；setAddress(null) 只清地址保端口；setSocketAddress(null) 精确 "unsupported address type"；ctor(null SA) IAE |
| testSetLengthCapacityMatrix | ctor(buf,5) 可长到 buf.length、超容 IAE；offset 参与（2+8=10 合法、2+9 IAE）；setData 换大缓冲后容量随之放大；getData().length 不随收缩 |
| testSetDataMatrix | setData(byte[]) 重置 offset=0/length=buf.length 且保留地址端口；setData(b,o,l) 合法边界与非法 4 格；活缓冲双向可见；收缩保引用 |
| testAddressPortMatrix | setSocketAddress/setAddress/setPort 正交性、getSocketAddress 分量断言、port 65535 替换 |
| testObjectContracts | equals 身份语义（未覆写）、hashCode 稳定、toString 含类名 |
| testOffsetLengthAcceptanceMatrix | 7×7 offset/length 全组合表驱动：合法 iff off≥0 ∧ len≥0 ∧ off+len≤buf.length，逐格断接受性 + 合法格精确值 |
| testNullAddressSocketAddressForm | setAddress(null)+setPort(7) → getSocketAddress() 为 wildcard:7（双侧实测） |
| testStructuralReflection | 恰好 6 个公开构造器；6 访问器 + 4 setter 全 public synchronized；final 类 |
| testLoopbackRoundTrip | offset 窗口 send（[1,5)=FE BA BE 42）→ receive 填源地址/端口、length 收缩、offset 保留、窗口外字节 0x7F 不动；零长数据报；复用包再收 |

## 版本差异（探针实测）

- length/offset 类 IAE 消息：JDK-8 "illegal length or offset"/"illegal length" vs
  JDK-27 "Range [a, a + n) out of bounds for length L" → 双侧只断类型；
  **接受/拒绝边界本身完全一致**。
- 端口消息 "Port out of range:<v>"、setSocketAddress(null) "unsupported address type" 双侧一致 → 断精确文本。
- 默认端口：JDK-8 -1（getSocketAddress 抛 IAE）vs JDK-27 0（wildcard:0）。
