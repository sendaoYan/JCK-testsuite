# JVMS 4.9 Constraints on JVM Code 测试设计

## 1. 覆盖的静态约束

JVMS 4.9 要求每条分支指令的目标落在 code 数组内、局部变量/操作数栈的使用
不越过 max_locals/max_stack 声明、操作码必须有定义。分支偏移编码：
goto/if*_ 二字节 s2（[-32768, 32767]），goto_w/jsr_w 与 tableswitch/
lookupswitch 的偏移为 s4，因此 |offset| > 32767 的跳转只能由宽形式编码。

## 2. 测试清单

| 测试 | 断言内容 |
|---|---|
| BranchTargetTest | goto +32767 越过 code 末端 → VerifyError/ClassFormatError（既有, jasm） |
| IllegalOpcodeTest | 未定义操作码 0xCA → VerifyError（既有, jasm） |
| LocalVariableOverflowTest | istore 越过 max_locals → VerifyError（既有, jasm） |
| GotoWideOffsetTest | goto 恰 +32767（s2 上界）落在末条 return → 验证并执行；前向 goto_w +40000（s2 溢出必须用宽形式）→ 执行返回 1；后向 goto_w -40007 循环 → 验证并终止；tableswitch 匹配偏移 +39999 → 派发返回 5 |

本次补缺（既有目录只覆盖越界负例，无 goto_w/switch 正例）自研实现：
DataOutputStream 手写 class 字节 + 手写 StackMapTable。

## 3. 手写分支代码的关键坑（实证）

1. class 版本 ≥ 50 的分支代码必须带 StackMapTable，否则类型检查验证器直接
   VerifyError（Operand stack underflow / Expecting a stack map frame）；
   无分支的直线代码不需要。
2. frame 必须放在**每个基本块起点**：不只分支目标，还包括分支指令之后
   （含不可达死代码块）的第一条指令。
3. full_frame（frame_type=255）的 number_of_locals / number_of_stack_items
   是 u2 不是 u1；首个 frame 的 offset 即 delta，其后 offset = prev + delta + 1。
4. tableswitch 的补位按 code 数组起始对齐（4 字节边界），偏移相对 switch
   指令地址。
5. if*/goto 的 s2 限制意味着跨大间隔（> 32767 字节）的条件跳转本身非法：
   条件判断要放在间隔之后，宽间隔只由 goto_w 跨越。
6. jsr/jsr_w 已过时（JVMS 4.10.1.7 子程序语义），本目录不为其建正例。

## 4. 版本差异

无：全部四个正例在 JDK-8 jdk8u372-b07 与 JDK-27 27.0.1.0.2 上加载并执行
结果一致。生成类为版本 52.0。

## 5. 运行纪律

纯 headless @run main；每文件独立 workDir；jtreg 双侧 0 fail；
既有测试一律不改不删。
