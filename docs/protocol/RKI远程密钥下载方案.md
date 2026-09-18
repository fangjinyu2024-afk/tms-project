# 远程密钥下载方案（RKI）

> 由《远程密钥下载方案（RKI）.docx》转换，内容与原文一致，仅补齐表格格式；原文末尾的 RKI 泳道图为绘图对象，转换后文字次序错乱，以原文图形为准。

远程密钥下载方案
Version History:

| Version | Date | Description | Author |
| --- | --- | --- | --- |
|  |  |  |  |
|  |  |  |  |
|  |  |  |  |

## 概述
远程密钥注入必须符合PCI和UPTS规范。使用公钥方法的RKI技术需要遵守PCI定义的密钥大小标准以及主机和设备之间的相互身份验证。下载对称密钥时，使用PKI和X.509证书来确保下载的TR-31 KBPK密钥的安全性。当从共享对称KBPK(或 KEK)加密的远程主机对称密钥时，使用TR-下载31或等效方法。
## 术语

| 术语 | 说明 |
| --- | --- |
| KEK | A key used to encryption or decryption other key |
| KBPK | The key used to protection other key. |
| TR-31 | X9 TR-31 2010 |
| KDH | Key-distribution Host |
| RKI | Remote Key Injection |
| PKI | Public key Infrastructure |
| TLS | Transport Layer Security |
| CSR | Certificate Signing Request |

## 方案实施
设备使用远程密钥注入RKI(Remote Key Injection)下载对称密钥的保护密钥（KEK or KBPK）。这些密钥在分发过程中被PKI(Public key Infrastructure) X509证书保护。
该过程向终端分配三种密钥：
□ DUKPT IPEK
□ MK
□ Fixed Key
□ RSA Key
RKI使用ANSIX9 TR-31分发对称密钥。使用TR-31 Binding技术，通过(KEK or KBPK) 加密和认证的要下载的对称密钥。
使用KBPK为AES256.
## TR31的结构体
KBH:按照标准填写。
KBH(option)：
## KBH(option)字段的格式

| 字段 | 格式 | 长度 | 备注 |
| --- | --- | --- | --- |
| ID | AN | 2 | DUKPT时必须存在 |
| Length | AN | 2 | Hex格式的长度用ASCII表示，比如长度为24字节，16进制为：0x18，则表示为：0x31，0x38 |
| Content | AN | Var | 上送的内容 |

## ID的定义说明

| ID | 内容 | 备注 |
| --- | --- | --- |
| KS | KSN | DUKPT时必须存在 |
| KM | 密钥内容 | 必须存在 |
| BP | 补位数据 | 用于将KBH(option)字段补位到16字节倍数 |

## 当ID=KM，密钥内容说明

| 字段 | 格式 | 长度 | 说明 |
| --- | --- | --- | --- |
| 密钥体系 | AN | 2 | 00: DUKPT01: MSK02: Fixed Key10: Term_TLS_PK11: Term_TLS_SK12: Term_RKI_PK13: Term_RKI_SK |
| 密钥算法 | AN | 2 | 00: TDE3S01: AES02: SM410: RSA11: ECC12: SM2 |
| 密钥索引 | AN | 2 | 00-FF |
| Reseverd | AN | 6 | 固定为全0 |

密钥远程下载步骤分为工厂证书注入Factory Certificate Injection和Merchant Remote Key Injection (RKI)两个阶段。
Factory Certificate Injection： A
Merchant Remote Key Injection (RKI):  B, C, D, E, F
设备在部署到商户之前，需要在安全屋内下载一对双向认证用的私钥和公钥证书（Term_TLS_PK,Term_TLS_SK） 和 一对RKI的证书(Term_RKI_PK,Term_RKI_SK)。
RKI开始之前，设备和KDH需要进行互相认证，使用安全协议比如TLSv1.2建立安全信道。
当设备部署到商户时，设备发送一个RKI请求。
这个请求数据包括：
公钥Term_RKI_PK.
密钥种类+证书下发方式
Devices 终端信息
设备产生32B RandomA.
设置产生的8B RandomT
终端可以接收的最大数据长度
终端用RKI_CA 对数据进行加密。
终端用Term_RKI_SK对数据进行签名。
当KDH收到RKI请求后，会做以下工作：
保存公钥Term_RKI_PK，解密密文
验证签名。
保存终端信息和随机数RandomA, RandomT
产生TR-31保护密钥KBPK。
32B RandomB.
8B RandomR
用Term_RKI_SK 加密以上数据
密钥的总个数
同时，KDH将RandomA xor RandomB = TransKey 保存，用于后续数据的加解密。
当设备接收到RKI回复，完成以下工作：
解密数据。
验证签名。
获取randomA，RandomR， KBPK。
终端将RandomA xor RandomB = TransKey 保存，用于后续数据的加解密。
同时保存KBPK，用于密钥数据的解密和Mac验证。
终端发起密钥下载请求,数据内容包括
终端状态，密钥索引,Nexpack, KCV， RandomR
用Transkey加密。
说明：密钥索引，终端上送的密钥索引是指请求下发第n个密钥。n=0,表示从第1个开始下载。如果n !=0, 且是认证后的第一次上送，则n必须为最后一次成功上送kcv后的密钥索引值+1.(如果不确定是否成功，则直接上送最后一次成功的索引)
KCV：如果是认证后的首次上送，则KCV=全0。否则KCV=密钥的CV值（对称密钥）或者0（非对称密钥成功），0xFFFFFFFF(非对称密钥失败)
KDH收到后，将进行以下处理
解密数据
判断终端状态，如果状态异常，则下发错误信息，并断开连接。
判断NextPack，如果Nextpack与服务器的nexpack状态不一致，则返回错误，一致，如果NextPack==0，则跳过步骤4），不为0，则判断密钥索引是否为当前密钥索引，如果不是，则返回错误。否则，跳到步骤7）。
判断密钥索引是否合法，如果该索引号之前有未上送KCV的，则从最小的索引值开始下发密钥。
判断KCV，如果是认证后第一次上送，则不处理.如果下发的是对称密钥，则校验 CV值，非对称密钥，CV=0，则表示成功。
如果第5）验证失败，则重发当前密钥。如果已重发3次，则结束。如果5）验证成功，判断是否下发完成，未完成，则准备新密钥，完成，则准备结束包
准备分包数据或者新密钥数据或者结束包。
KDH 将重复 F 到 G 直到所有密钥下载完成。
当设备收到RKI done 命令
终端验证下发密钥的合法性和完整性。
如果有更新非对称密钥，则启用新的非对称密钥。
## RKI泳道图
DevicesDevicesKDHKDHCA ServerCA Server
CSR证书请求CSR证书请求
产生公私钥对Term_TLS_SK,Term_TLS_PKTerm_RKI_SK,Term_RKI_PK产生公私钥对Term_TLS_SK,Term_TLS_PKTerm_RKI_SK,Term_RKI_PK
下发证书Term_TLS_SK,Term_TLS_PKTerm_RKI_SK,Term_RKI_PK下发证书Term_TLS_SK,Term_TLS_PKTerm_RKI_SK,Term_RKI_PK下发TLS公私钥term_TLS_SK,Term_TLS_PK下发TLS公私钥term_TLS_SK,Term_TLS_PK
H．密钥下发完毕，清除敏感信息H．密钥下发完毕，清除敏感信息H．密钥下发完毕，清除敏感信息H．密钥下发完毕，清除敏感信息G. KDH下发密钥。G. KDH下发密钥。上送KCV+密钥索引等信息上送KCV+密钥索引等信息F.终端请求密钥下载。F.终端请求密钥下载。E.终端验证解密数据并验证签名。认证完成。E.终端验证解密数据并验证签名。认证完成。C. RKI认证请求。用RKI_CA_PK加密数据，并用Term_RKI_SK签名数据包含：RandomA+RandomT+下载内容+终端信息等C. RKI认证请求。用RKI_CA_PK加密数据，并用Term_RKI_SK签名数据包含：RandomA+RandomT+下载内容+终端信息等D. KDH保存临时公钥PUrki和 RanomA，RandomT。D. KDH保存临时公钥PUrki和 RanomA，RandomT。KDH用公钥Term_RKI_PK加密数据，并用RKI_CA_SK签名。  数据包含为：RandomB + KBPK+ RandomR+密钥数量KDH用公钥Term_RKI_PK加密数据，并用RKI_CA_SK签名。  数据包含为：RandomB + KBPK+ RandomR+密钥数量下发RKI公私钥Term_RKI_SK,Term_RKI_PK下发RKI公私钥Term_RKI_SK,Term_RKI_PK工厂工厂B. TLSV1.2双向认证，发送证书CERTdev带PUdevB. TLSV1.2双向认证，发送证书CERTdev带PUdevTLSV1.2双向认证，发送证书CERTkdh带PUkdhTLSV1.2双向认证，发送证书CERTkdh带PUkdhRKI结束RKI结束商用部署商用部署

