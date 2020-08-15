package org.hyperledger.fabric.samples.test;

import org.hyperledger.fabric.samples.Main;
import org.hyperledger.fabric.samples.gm.sm9.*;

import java.security.MessageDigest;

public class CrossServiceDomain {
    public static void test() throws Exception{
        // 服务域1
        SM9Curve sm9Curve1 = new SM9Curve();
        KGC kgc1 = new KGC(sm9Curve1);
        SM9 sm91 = new SM9(sm9Curve1);

        // 服务域2
        SM9Curve sm9Curve2 = new SM9Curve();
        KGC kgc2 = new KGC(sm9Curve2);
        SM9 sm92 = new SM9(sm9Curve2);

        //服务域1
        String id_device1 = "device1"; //统一设备标识符

        String serviceDomain = "医院服务";
        String province = "安徽省";
        String city = "合肥市";
        String hospital = "合肥市第二人民医院";
        String deviceType = "医疗救护车";
        String info1 = serviceDomain + ":" + province + ":" + city + ":" + hospital + ":" + deviceType; // 网关管理员在部署设备时填写的信息
        MessageDigest sha256Digest1 = MessageDigest.getInstance("SHA-256");
        sha256Digest1.update(info1.getBytes());
        byte [] digestInfo1 = sha256Digest1.digest();


        String message1 = "随机消息1"; //发起认证时临时生成的随机消息

        MasterKeyPair masterKeyPair1 = kgc1.genSignMasterKeyPair(); //服务域 1的KGC生成服务域内的系统主密钥对
        PrivateKey privateKey1 = kgc1.genPrivateKey(masterKeyPair1.getPrivateKey(), id_device1 + digestInfo1,
                PrivateKeyType.KEY_SIGN); // 服务域1的KGC给设备生成签名私钥
        ResultSignature signature1 = sm91.sign(masterKeyPair1.getPublicKey(), privateKey1,
                (id_device1 + info1 + message1).getBytes());


        //服务域2
        MasterPublicKey masterPublicKey_from_service1 = MasterPublicKey.fromByteArray(kgc1.getCurve(),
                masterKeyPair1.getPublicKey().toByteArray()); //服务域2获取服务域1的主公钥
        ResultSignature signature1_from_service1 = ResultSignature.fromByteArray(kgc1.getCurve(),
                signature1.toByteArray()); //服务域获取服务域1设备的签名信息；
        boolean ok = sm92.verify(masterPublicKey_from_service1, id_device1 + digestInfo1,
                (id_device1 + info1 + message1).getBytes(), signature1_from_service1);


        if (ok) {
            Main.showMsg("服务域2认证服务域1设备标识成功！");
            Main.showMsg("被验证设备身份信息：");
            Main.showMsg(info1);
        }
    }

    public static void main(String[] args) throws Exception{
        new CrossServiceDomain().test();
    }
}
