package org.hyperledger.fabric.samples.test;

import org.hyperledger.fabric.samples.gm.sm9.*;
import org.hyperledger.fabric.samples.util.Hex;

import java.io.PrintStream;
import java.net.Socket;
import java.security.SecureRandom;
import java.util.concurrent.TimeUnit;

public class ServiceDomainB {
    public static void main(String[] args) throws Exception{
        SM9Curve sm9Curve = new SM9Curve();
        KGC kgc = new KGC(sm9Curve);
        SM9 sm9 = new SM9(sm9Curve);

        try {
            while (true) {
                String deviceID = "stone";
                System.out.println("设备标识：" + deviceID);

                String timestamp = String.valueOf(System.currentTimeMillis());
                String attachInfo = "CS.HFUT.EDU" + "." + timestamp;

                String verificationKey = deviceID + "." + attachInfo;

                System.out.println("设备公钥：" + verificationKey);

                SecureRandom random = new SecureRandom();
                byte[] bytes = new byte[32];
                random.nextBytes(bytes);
                String byteStr = Hex.encodeToString(bytes);
                System.out.println("消息随机数：" + byteStr);

                String message = byteStr;


                MasterKeyPair masterKeyPair = kgc.genSignMasterKeyPair();
                PrivateKey signatureKey = kgc.genPrivateKey(masterKeyPair.getPrivateKey(), verificationKey, PrivateKeyType.KEY_SIGN);
                ResultSignature signature = sm9.sign(masterKeyPair.getPublicKey(), signatureKey, message.getBytes());


                // 部分信息传输前，先转换成十六进制字符串
                String hexStrMasterPublicKey = Hex.encodeToString(masterKeyPair.getPublicKey().toByteArray());
                String hexStrSignature = Hex.encodeToString(signature.toByteArray());

                // Socket通信
                Socket socket = new Socket("localhost", 63363);
//                Socket socket = new Socket("192.168.1.4", 4000);
                PrintStream printStream = new PrintStream(socket.getOutputStream());

                String signal = "REQUEST";
                printStream.println(signal); // 第一行，传输认证请求
                System.out.println("认证请求信号：" + signal);

                printStream.println(hexStrMasterPublicKey); // 第二行，传输本域系统主公钥
                System.out.println("服务域主公钥：" + hexStrMasterPublicKey);

                printStream.println(hexStrSignature); // 第三行， 传输签名
                System.out.println("消息签名：" + hexStrSignature);

                printStream.println(verificationKey); // 第四行，传输验证公钥
                System.out.println("签名验证公钥：" + verificationKey);

                printStream.println(message); // 第五行，传输消息
                System.out.println("认证消息：" + message);

                // 申请动作
                printStream.println("DEVICE");
                printStream.println("door");
                printStream.println("open");
                printStream.println("none");

                printStream.println("OVER"); //最后一行（末尾4个字节），指示消息传输结束
                System.out.println("请求认证结束标示符：" + "OVER");

                printStream.close();
                socket.close();

                TimeUnit.SECONDS.sleep(30); // 每1分钟发送一次认证请求
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
