/*
 * SPDX-License-Identifier: Apache-2.0
 */

package org.hyperledger.fabric.samples.authentication;

import com.alibaba.fastjson.JSONObject;
import com.owlike.genson.Genson;
import org.hyperledger.fabric.contract.Context;
import org.hyperledger.fabric.contract.ContractInterface;
import org.hyperledger.fabric.contract.annotation.*;
import org.hyperledger.fabric.samples.gm.sm9.*;
import org.hyperledger.fabric.samples.util.Hex;
import org.hyperledger.fabric.samples.util.Sha256Util;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.util.Date;

/**
 * Java implementation of the IOT Authentication Contract
 */
@Contract(
        name = "Authentication",
        info = @Info(
                title = "Authentication contract",
                description = "The Authentication contract",
                version = "2.2",
                license = @License(
                        name = "Apache 2.0 License",
                        url = "http://www.apache.org/licenses/LICENSE-2.0.html"),
                contact = @Contact(
                        email = "sunrungeng@163.com",
                        name = "Sun Rungeng",
                        url = "https://github.com/sunrungeng/")))
@Default
public final class Authentication implements ContractInterface {

    private final Genson genson = new Genson();
    private static SM9Curve sm9Curve;
    private static KGC kgc;
    private static SM9 sm9;

    static {
        sm9Curve = new SM9Curve();
        kgc = new KGC(sm9Curve);
        sm9 = new SM9(sm9Curve);
    }

    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        String[] userData = {
                "{ \"belongto\": \"alice\", \"identity\": \"China\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"0112afedcd6d61818beb83505ad9a7b97501b116eec59f6c4b7e07ef0ac7bc6f6d043246f8f2b8b60da8b28a40984c8ba7dca7a3b44ec9671e0a66525d0a0e87bb288e627616382433457d8e3309ad00a35c35d679cddb2632027ad51893f3122b4543c88549184063f1a1980a047d58252095f29891d1cd61427e9eee95105725\" ,\"addTime\" : \"1599029260611\" }",
                "{ \"belongto\": \"bob\", \"identity\": \"US\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"017f224cd143f3feea6483e54c92200b2ba2d1f44f5a74127d6f5c3480067c88d42f7b2b8576ea0305abe79f5d443f485aaeebe1517933221c005e497f9c58cbe802bf622fb94ec38bc937f78c957b1f81816e7a3ae3c4814ab0ee3aab6beb47e896f9a2aabcbcb541ec28d0f01a6a3f53f2938af762bd09321c7589cf10280d5c\" ,\"addTime\" : \"1599029163611\" }",
                "{\"addTime\":\"1599029263611\",\"belongto\":\"\",\"deviceCount\":0,\"idCount\":0,\"identity\":\"cn\",\"publicParameter\":\"01675ec74252520c5833adae367a7ecc24b4e94ff6cf54941cc516f4518e4af7901049702e0739580d473d1977d3c1b3615142692a5f82d922ce248e9f22442a2db3b6fea172bf14f85c4e43420702dff4db29de6d29c5ad7f252b7de28850863167d42382e096c49f0309f36827dbf40e1cfaf3963eb5dfcfab044a474c26b4af\",\"zone\":\"\"}",
                "{\"addTime\":\"1599029263681\",\"belongto\":\"\",\"deviceCount\":0,\"idCount\":0,\"identity\":\"edu.cn\",\"publicParameter\":\"019d8815caf8b5628a29f6164f70996ac85d596b61cfc46a48bbb961100c29e49bab124bcc0352a78f49fc4c4cd202ffceaff7c0ac1d6939eda10d658183f926419adeae618ed271cf225248794530e7b7c563909d63133c7b79876ba53b67691fa727bc8092661b3a6dfe8d9caebf88ddd8c2122127f70ebbbd67e7adb2432597\",\"zone\":\"cn\"}",
                "{\"addTime\":\"1599029263981\",\"belongto\":\"\",\"deviceCount\":0,\"idCount\":0,\"identity\":\"tsinghua.edu.cn\",\"publicParameter\":\"0197d9144ba59f7f2f175fc4a7890406c4a5c5f90c235b632e849c7c3410fc1fe43ba31b914b366d0be77b5c3353ae61aa142cb5b033b3af23da0771b9c9c4fd699d5191167b7d71e31b7eff8cefe201d9f82d28b839564b09fa72e6acfe6f51b10cd578c737def1a7b62712a77b579350a6898165d45dba1b7c44ddfe06233646\",\"zone\":\"edu.cn\"}"

        };

        for (int i = 0; i < userData.length; i++) {
            User user = genson.deserialize(userData[i], User.class);
            String key = user.getIdentity();
            String carState = genson.serialize(user);
            stub.putStringState(key, carState);
        }
    }

    /**
     * @param ctx
     * @param ID TopKGC的id
     * @param MPK TopKGC的MPK
     * @return
     */
    @Transaction()
    public String registerTop(final Context ctx, String ID, String MPK) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        JSONObject object = new JSONObject();
        if(!userState.isEmpty()) {
            String errorMessage = String.format("Node %s already exist", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node already exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        }
        Date date = new Date();
        String addTime = date.toString();
        User user = new User("", ID, "", 0, 0, MPK, addTime);
        stub.putStringState(ID, genson.serialize(user));
        object.put("errcode", 0);
        object.put("message", "success");
        return object.toString();
    }

    /**
     *
     * @param ctx
     * @param IDi 上级KGC节点ID
     * @param IDi_1 要注册的节点ID
     * @return
     */
    @Transaction()
    public String registerNotTop(final Context ctx, String IDi, String IDi_1) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(IDi);
        JSONObject object = new JSONObject();
        if (userState.isEmpty()) {
            String errorMessage = String.format("Node %s does not exist", IDi);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        userState = stub.getStringState(IDi_1);
        if (!userState.isEmpty()) {
            String errorMessage = String.format("Node %s already exist", IDi);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node already exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        }
        Date date = new Date();
        String addTime = date.toString();
        User user = new User("", IDi_1, "", 0, 0, "", addTime);
        stub.putStringState(IDi_1, genson.serialize(user));
        object.put("errcode", 0);
        object.put("message", "success");
        return object.toString();
    }

    /**
     * @param ctx
     * @param ID 节点id
     * @return 节点所在自己域的公共管理参数即MPK
     */
    @Transaction()
    public String queryMPK(final Context ctx, String ID) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        JSONObject object = new JSONObject();
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "fail");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        object.put("errcode", 0);
        object.put("message", "success");
        object.put("data", user.getPublicParameter());
        return object.toString();
    }

    @Transaction()
    public String verifyAndUpload(final Context ctx, String IDi, String IDi_1, String message, String signature) {
        JSONObject object = new JSONObject();
        boolean result = false;
        // 根据ID获取父节点的MPK
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(IDi);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", IDi);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        String publicPara = user.getPublicParameter();
        MasterPublicKey masterPublicKey = MasterPublicKey.fromByteArray(sm9Curve, Hex.decode(publicPara));

        // 根据MPKi, IDi 对 sign 进行验证
        byte[] byteMessage = message.getBytes();
        ResultSignature resSignature = ResultSignature.fromByteArray(sm9Curve, Hex.decode(signature));
        result = sm9.verify(masterPublicKey, IDi_1, byteMessage, resSignature);
        if(result == true) {
            JSONObject object1 = JSONObject.parseObject(message);
            String mpk = object1.getString("mpk");
            userState = stub.getStringState(IDi_1);
            User user1 = genson.deserialize(userState, User.class);
            User user2 = new User(user1.getBelongto(), user1.getIdentity(), IDi, user1.getIdCount(), user1.getDeviceCount(), mpk, user1.getAddTime());
            stub.putStringState(user2.getIdentity(), genson.serialize(user2));
            object.put("errcode", 0);
            object.put("message", "success");
            object.put("data", mpk);
            return object.toString();
        } else {
            object.put("errcode", 1);
            object.put("message", "verify failed");
            return object.toString();
        }
    }

    @Transaction()
    public String verifyAndRegisterNotTop(final Context ctx, String domain, String identity, String type, String time,
                                          String signature) {
        JSONObject object = new JSONObject();
//        JSONObject message = new JSONObject();
//        message.put("domain", domain);
//        message.put("identity", identity);
//        message.put("type", type);
//        message.put("time", time);
        String message = domain + "," + identity + "," + type + "," + time;
        String hashMessage = Sha256Util.getSHA256(message);

        boolean result = false;
        // 根据ID获取父节点的MPK
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(domain);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", domain);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
        }
        User user = genson.deserialize(userState, User.class);
        String publicPara = user.getPublicParameter();
        MasterPublicKey masterPublicKey = MasterPublicKey.fromByteArray(sm9Curve, Hex.decode(publicPara));

        // 根据MPKi, IDi 对 sign 进行验证
        ResultSignature resSignature = ResultSignature.fromByteArray(sm9Curve, Hex.decode(signature));
        result = sm9.verify(masterPublicKey, identity, hashMessage.getBytes(), resSignature);
        if(result == true) {
            Date date = new Date();
            String addTime = date.toString();
            object.put("message", "verify success");
            return object.toString();
        } else {
            object.put("errcode", 1);
            object.put("message", "verify failed");
            return object.toString();
        }
    }

    @Transaction
    public String updateMPK(final Context ctx, String ID, String MPK) {
        JSONObject object = new JSONObject();
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
        }
        User user = genson.deserialize(userState, User.class);
        User user1 = new User(user.getBelongto(), user.getIdentity(), user.getZone(), user.getIdCount(), user.getDeviceCount(), MPK, user.getAddTime());
        String newUserState = genson.serialize(user1);
        stub.putStringState(ID, newUserState);
        object.put("errcode", 0);
        object.put("message", "success");
        return object.toString();
    }

    @Transaction()
    public String queryZone(final Context ctx, String ID) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        JSONObject object = new JSONObject();
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "fail");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        object.put("errcode", 0);
        object.put("message", "success");
        object.put("data", user.getZone());
        return object.toString();
    }

    @Transaction()
    public String queryZoneMPK(final Context ctx, String ID) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        JSONObject object = new JSONObject();
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        String zone = user.getZone();
        userState = stub.getStringState(zone);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s zone not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node's zone not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User zoneUser = genson.deserialize(userState, User.class);
        object.put("errcode", 0);
        object.put("message", "success");
        object.put("data", zoneUser.getPublicParameter());
        return object.toString();
    }

    @Transaction()
    public String queryZoneInfo(final Context ctx, String ID) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        JSONObject object = new JSONObject();
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        String zone = user.getZone();
        userState = stub.getStringState(zone);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s zone not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node's zone not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User zoneUser = genson.deserialize(userState, User.class);
        object.put("errcode", 0);
        object.put("message", "success");
        object.put("data", userState);
        return object.toString();
    }

    @Transaction()
    public String queryZoneMPKAndID(final Context ctx, String ID) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        JSONObject object = new JSONObject();
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        String zone = user.getZone();
        userState = stub.getStringState(zone);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s zone not found", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node's zone not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User zoneUser = genson.deserialize(userState, User.class);
        object.put("errcode", 0);
        object.put("message", "success");
        object.put("identity", zoneUser.getIdentity());
        object.put("mpk", zoneUser.getPublicParameter());
        return object.toString();
    }

    @Transaction()
    public String verifyDevice(final Context ctx, String accesserIdentity, String accessedDevice, String action,
                         String time, String signature) {
        JSONObject object = new JSONObject();
        JSONObject result = new JSONObject();
        object.put("accesserIdentity", accesserIdentity);
        object.put("accessedDevice", accessedDevice);
        object.put("action", action);
        object.put("time", time);
        String message = accesserIdentity +","+ accessedDevice +","+ action +","+ time;
//        String message = "accesserIdentity" + accesserIdentity +
        String hashMessage = Sha256Util.getSHA256(message);
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(accesserIdentity);
        if (userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", accesserIdentity);
            result.put("errcode", 1);
            result.put("message", errorMessage);
            return result.toString();
        }
        User user = genson.deserialize(userState, User.class);
        String zone = user.getZone();
        userState = stub.getStringState(zone);
        if (userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", zone);
            result.put("errcode", 1);
            result.put("message", errorMessage);
            return result.toString();
        }
        User zoneUser = genson.deserialize(userState, User.class);
        String MPK = zoneUser.getPublicParameter();

        boolean verifyResult = sm9.verify(MasterPublicKey.fromByteArray(sm9Curve, Hex.decode(MPK)), accesserIdentity,
                hashMessage.getBytes(), ResultSignature.fromByteArray(sm9Curve, Hex.decode(signature)));
        if (verifyResult) {
            result.put("errcode", 0);
            result.put("message", "verify success");
            result.put("accesserIdentity", accesserIdentity);
            result.put("accessedDevice", accessedDevice);
            result.put("action", action);
            result.put("add_time", time);
            result.put("signature", signature);
            result.put("accessMessage", message);
            result.put("accesserDomain", zone);
            result.put("accesserDomainMpk", MPK);
            result.put("is_auth", "true");
            return result.toString();
        } else {
            result.put("errcode", 1);
            result.put("message", "verify fail");
            result.put("hashMessage", hashMessage);
            result.put("message2", message);
            result.put("zone", userState);
            return result.toString();
        }
    }


    @Transaction
    public String insert(final Context ctx, String str) {
        JSONObject object = new JSONObject();
        User user = genson.deserialize(str, User.class);
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(user.getIdentity());
        if(!userState.isEmpty()) {
            String errorMessage = String.format("Node %s already exist", user.getIdentity());
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node already exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        } else {
            stub.putStringState(user.getIdentity(), genson.serialize(user));
            object.put("errcode", 0);
            object.put("message", "success");
            return object.toString();
        }
    }

    @Transaction
    public String update(final Context ctx, String str) {
        JSONObject object = new JSONObject();
        User user = genson.deserialize(str, User.class);
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(user.getIdentity());
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not exist", user.getIdentity());
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        } else {
            stub.putStringState(user.getIdentity(), genson.serialize(user));
            object.put("errcode", 0);
            object.put("message", "success");
            return object.toString();
        }
    }

    @Transaction
    public String query(final Context ctx, String ID) {
        JSONObject object = new JSONObject();
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        if (userState.isEmpty()) {
            String errorMessage = String.format("Node %s not exist", ID);
            System.out.println(errorMessage);
            object.put("errcode", 1);
            object.put("message", "Node not exist");
            return object.toString();
//            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        } else {
            object.put("errcode", 1);
            object.put("message", "success");
            object.put("data", userState);
            return object.toString();
        }
    }


}
