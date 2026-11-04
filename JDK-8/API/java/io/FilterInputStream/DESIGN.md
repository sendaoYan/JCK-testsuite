# java.io.FilterInputStream 测试设计案（JDK-8）

> 覆盖目标：**完整契约**。装饰器基类：read()/read(byte[],int,int)/skip/available/
> close/mark/reset/markSupported 全部透传；子类覆写拦截；protected `in` 访问；
> 底层异常透传；null 委托在首次使用时抛 NPE。

## 1. API 提供了哪些功能

- **构造器**：`protected FilterInputStream(InputStream in)`。
- **透传**：`read()`、`read(byte[],int,int)`、`skip(long)`、`available()`、`close()`、`mark(int)`、`reset()`、`markSupported()` 全部委托给底层 `in`。
- **字段**：`protected volatile InputStream in`（子类可访问/替换）。
- **语义**：构造器**不**校验 null 委托；首次 read 才抛 NPE。

## 2. Java 用户如何使用

- 作为自定义装饰器（计数、缓冲、转换）的基类，覆写需要拦截的方法。
- 依赖 protected `in` 字段直接访问被包装流。

## 3. 参考来源（不凭记忆编造）

- **Oracle Java SE 8 javadoc**（`java/io/FilterInputStream.html`）：所有方法委托契约。
- **JDK 实现源码**（`src/share/classes/java/io/FilterInputStream.java`）：构造器只赋值 `in`、`read()` 直接 `in.read()`（null 时 NPE）。

## 4. 测试用例设计（映射到 FilterInputStreamTest.java 方法）

| 优先级 | 测试点 | 用例方法 |
|---|---|---|
| P0 | read/read(byte[])/available/skip/close 全部透传 | testDelegation |
| P1 | markSupported/mark/reset 透传 | testMarkReset |
| P1 | 子类覆写拦截（计数装饰器） | testSubclassInterception |
| P1 | protected `in` 字段访问 | testProtectedIn |
| P1 | 底层异常透传（消息保留） | testFailingDelegate |
| P1 | null 委托在首次 read 抛 NPE | testNullDelegate |

## 关键规范细节（易错，已锁入断言）

- 构造器**不**校验 null 委托（`new FilterInputStream(null){}` 不抛），首次 read 才抛 NPE。
- `close()` 透传到底层 close；`skip` 透传并受底层钳制。
- 子类可覆写 `read()` 并在 `super.read()` 前后计数/转换。

## 注意事项

- 全部输入为固定字节数组，无墙钟/随机/Locale/Charset/文件/网络依赖，headless 安全。
- JDK-27 侧同目录额外保留反射白盒（protected 构造器、protected volatile in 字段）。

## 深化追加（契约级）

本轮（批次 ioA）追加契约级断言（13 -> 66）。

### 新增维度
- read(byte[]) 路由：经自身 3 参 read 下沉为 1 次 bulk、0 次单字节；空数组仍委托一次返回 0；read(null) NPE。
- 不支持 mark 的下沉 reset -> IOException "mark/reset not supported"（双侧一致）；mark 无害无操作。
- 参数校验委托给下沉（OOB/NPE 由下沉抛出，流仍可用）。
- close 传播：一层/两层；第二次 close 仍到达下沉（双侧实测 closes==2，FIS 无 closed 标志）。
- skip 语义：skip(0)/skip(-5) 委托返回 0；skip(2) 委托。
- available 每次调用计数委托。
- EOF 语义四操作；protected in 中途换下沉（读 1 后换，续读 9）。
- 下沉全操作失败透传（read/read(byte[])/read(b,off,len)/skip/available/close 六路 "inner failure"）。
- null 下沉六操作全 NPE；无符号读（0xFF->255）；区域精确放置。

### 负例精确异常表（双侧实测）
| 操作 | 类型 | 消息子串 |
|---|---|---|
| reset（下沉不支持 mark） | IOException | "mark/reset not supported"（双侧一致） |
| read(b,-1,1) 等经委托 | IndexOutOfBoundsException | 消息由下沉决定（BAIS 路径 null） |
| 六操作 × null 下沉 | NullPointerException | 双侧消息 null |

### 版本差异
- 本类行为双侧一致（close 计数 2 亦双侧实测一致）。
