package com.hyj.tls13;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.crypto.KeyAgreement;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.util.HexFormat;

public class KeyExchangeDemo {

    private static final Logger log = LogManager.getLogger(KeyExchangeDemo.class);
    private static final HexFormat HEX = HexFormat.of();

    public static void main(String[] args) throws Exception {
        // 1. 给 Alice bob 各自生成一个密钥对
        KeyPairGenerator kpg = KeyPairGenerator.getInstance("X25519");
        KeyPair alice = kpg.generateKeyPair();
        KeyPair bob = kpg.generateKeyPair();

        // 2. 交换公钥

        // 3. Alice 的私钥 + bob 的公钥
        KeyAgreement aliceAG = KeyAgreement.getInstance("XDH");
        aliceAG.init(alice.getPrivate());
        aliceAG.doPhase(bob.getPublic(), true);
        byte[] aliceSecret = aliceAG.generateSecret();

        // 4. Bob 的私钥 + Alice 的公钥
        KeyAgreement bobAG = KeyAgreement.getInstance("XDH");
        bobAG.init(bob.getPrivate());
        bobAG.doPhase(alice.getPublic(), true);
        byte[] bobSecret = bobAG.generateSecret();

        // 5.对比
        log.info("Alice 算出: {}", HEX.formatHex(aliceSecret));
        log.info("Bob   算出: {}", HEX.formatHex(bobSecret));
        log.info("共享密钥长度: {} 字节", aliceSecret.length);
        log.info("公钥 getEncoded() 长度: {} 字节", alice.getPublic().getEncoded().length);
        log.info("私钥 getEncoded() 长度: {} 字节", alice.getPrivate().getEncoded().length);


    }
}
