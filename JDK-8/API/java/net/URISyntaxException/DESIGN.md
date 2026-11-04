# URISyntaxException 设计文档（JDK-8）

> 覆盖目标：完整契约——两个构造器的全部参数组合、getMessage 精确拼装格式、
> null/负 index 校验（NPE/IAE 优先级）、序列化稳定（serialVersionUID + 字段
> round-trip）、以及真实 URI 解析失败携带的精确三元组。2026-11-05 深化批次 A：
> 6 → 31 断言。

## 一、API 功能

- 构造器（2）：`(String input, String reason)`、`(String input, String reason, int index)`。
- 读取：`getInput()`、`getReason()`（即 super.getMessage()）、`getIndex()`（未给为 -1）。
- `getMessage()` 重写拼装：`reason` +（index > -1 时 `" at index N"`）+ `": "` + `input`。
- 继承 Exception（受检），serialVersionUID = 2137979680897488891L，串行字段 input/index 共 2 个。

## 二、Java 用户如何使用

`new URI(spec)` 解析失败时捕获，读取 getIndex() 定位错误位置、getReason() 取原因。

## 三、参考来源

- Oracle Java SE javadoc（java/net/URISyntaxException.html）。
- OpenJDK `java/net/URISyntaxException.java`（构造器校验顺序：null→NPE 先于 index→IAE）。
- 探针实测双 JDK：消息格式、serialVersionUID 完全一致。

## 四、测试用例设计（映射到 URISyntaxExceptionTest.java）

| 测试点 | 断言 |
|---|---|
| 2 构造器基本三元组 | input/reason/index 精确值；2 参 index=-1 |
| getMessage 精确格式 | `"missing :: bad input"`、`"missing : at index 3: bad input"`、index 0 也打印 |
| toString | `java.net.URISyntaxException: <message>` |
| null 校验 | null input / null reason / 双 null → NPE；`(in, null, -5)` → NPE（null 检查优先于 index 检查） |
| index 校验 | index < -1 → IllegalArgumentException 且 message 为 null |
| getCause | 默认 null；initCause 一次性设置生效 |
| 受检异常 | 非 RuntimeException、可作 Exception 捕获 |
| 真实解析失败 | `new URI("1abc:x")` → input/reason/index 精确三元组 |
| 序列化 | serialVersionUID==2137979680897488891L、2 个串行字段、round-trip 后三字段+消息保持 |

## 五、易错规范细节

- 消息拼装的 `": "` 分隔符在 reason 之后：reason `"missing :"` 会产生 `"missing :: bad input"`。
- index == -1 时**不打印** index 段；index == 0 打印。
- 两个构造器对 null 一律 NPE（包括 reason），负 index 只在 3 参构造器校验且晚于 null 检查。
- JDK-8 与 JDK-27 行为完全一致（本类无版本差异）。
