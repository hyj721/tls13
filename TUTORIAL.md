# 手写 TLS 1.3 教程计划

目标：用 Java 从零实现一个 TLS 1.3 客户端，连上真实服务器完成握手，发出 HTTP 请求并解密响应。重点是**理解协议**，所以密码学算法本身（X25519、AES、SHA）直接用 JDK 自带的实现，协议部分全部自己写。

- 密码套件：`TLS_AES_256_GCM_SHA384`（和 [tls13.xargs.org](https://tls13.xargs.org) 一致，方便逐字节对照）
- 密钥交换：X25519
- 环境：JDK 17、Maven、Log4j 2、JUnit 5

## 学习方式

每课只学一个概念，固定节奏：

1. **讲概念**：这一步要解决什么问题
2. **写代码**：自己实现
3. **验收**：跑通，能对照 xargs.org 的就逐字节对上
4. **提交**：`git commit -m "lesson N: xxx"`

git 历史就是学习记录。

## 进度

### 第一阶段：密码学积木（先练熟工具，还不碰网络）

| | 课 | 内容 | 学到什么 |
|---|---|---|---|
| ✅ | 1 | X25519 密钥交换 | 两个人怎样在公开信道上算出同一个秘密 |
| ⬜ | 2 | 公钥的原始格式 | 32 字节和 44 字节的区别，初识 ASN.1 |
| ⬜ | 3 | 哈希 SHA-384 | 什么是哈希；边喂数据边取中间结果（Transcript 的基础） |
| ⬜ | 4 | HMAC 和 HKDF | 怎样从一个密钥派生出更多密钥，自己实现 HKDF |
| ⬜ | 5 | HKDF-Expand-Label | TLS 对 HKDF 的封装；引入 JUnit，用 xargs.org 的数据验证 |
| ⬜ | 6 | AES-GCM 加解密 | 对称加密、nonce、AAD、认证 tag；解密 xargs.org 上的一条真实记录 |

### 第二阶段：字节和格式（TLS 在网络上长什么样）

| | 课 | 内容 | 学到什么 |
|---|---|---|---|
| ⬜ | 7 | 字节读写工具 | 大端整数和带长度前缀的字段，TLS 所有消息都由它们拼成 |
| ⬜ | 8 | 记录层格式 | 5 字节记录头；用 Socket 收发记录 |

### 第三阶段：握手（拼起积木，连上真实服务器）

| | 课 | 内容 | 学到什么 |
|---|---|---|---|
| ⬜ | 9 | 构造 ClientHello | 各个扩展的作用；🎯 **第一次发给 openssl 并收到回复** |
| ⬜ | 10 | 解析 ServerHello | 拿到服务器公钥，算出共享密钥 |
| ⬜ | 11 | 密钥调度 | 从共享密钥推导握手密钥，每个中间值都和 xargs.org 对上 |
| ⬜ | 12 | 解密服务器第一条加密记录 | nonce 与序列号、外层类型伪装；🎯 **解密成功 = 前面全对** |
| ⬜ | 13 | 握手消息切分 | 为什么一条记录里能装好几条消息 |
| ⬜ | 14 | 解析 Certificate | 拿到服务器证书和公钥 |
| ⬜ | 15 | 验证 CertificateVerify | 服务器怎样证明自己持有证书私钥 |
| ⬜ | 16 | Finished | 怎样证明握手没被篡改；校验服务器的，发出自己的 |

### 第四阶段：通信与收尾

| | 课 | 内容 | 学到什么 |
|---|---|---|---|
| ⬜ | 17 | 应用数据 | 切换到应用密钥，发 HTTP GET；🎉 **读到网页内容** |
| ⬜ | 18 | 收尾 | Alert 和 close_notify，忽略 NewSessionTicket，连真实网站 |

### 进阶（可选）

- ⬜ 用 Wireshark 加 SSLKEYLOGFILE 解密并查看自己的流量
- ⬜ 证书链验证（签发者、有效期、域名）
- ⬜ HelloRetryRequest
- ⬜ 会话恢复（PSK）
- ⬜ 自己写一个服务端

## 三个里程碑

1. **第 9 课**：收到真实服务器的回复，说明 ClientHello 格式正确。
2. **第 12 课**：解密第一条加密记录，说明密钥交换、哈希、HKDF、密钥调度、AES-GCM 全部正确。这是最大的坎。
3. **第 17 课**：读到网页内容，完成。

## 参考资料

- [RFC 8446](https://www.rfc-editor.org/rfc/rfc8446)：TLS 1.3 规范。第 4 章握手、第 5 章记录层、第 7 章密钥调度
- [tls13.xargs.org](https://tls13.xargs.org)：逐字节图解一次真实连接，每个中间值都有
- [RFC 7748](https://www.rfc-editor.org/rfc/rfc7748)：X25519
- [RFC 5869](https://www.rfc-editor.org/rfc/rfc5869)：HKDF

## 本地测试服务器（第 9 课起用）

```bash
openssl req -x509 -newkey rsa:2048 -nodes -keyout key.pem -out cert.pem -days 365 -subj "/CN=localhost"
openssl s_server -tls1_3 -accept 4433 -cert cert.pem -key key.pem -www -trace
```

`-trace` 会打印服务器解析到的每条消息，排错时很有用。
