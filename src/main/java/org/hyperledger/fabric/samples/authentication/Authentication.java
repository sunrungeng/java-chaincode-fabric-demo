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
import org.hyperledger.fabric.shim.ChaincodeException;
import org.hyperledger.fabric.shim.ChaincodeStub;

import java.io.UnsupportedEncodingException;
import java.util.Base64;
import java.util.Date;

/**
 * Java implementation of the IOT Authentication Contract
 */
@Contract(
        name = "Authentication",
        info = @Info(
                title = "Authentication contract",
                description = "The Authentication contract",
                version = "1.5",
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

    private enum Msg {
        SUCCESS,
        FAIL
    }

    private enum AuthErrors {
        NODE_KGC_NOT_FOUND,
        NODE_KGC_ALREADY_EXIST
    }

    @Transaction()
    public void initLedger(final Context ctx) {
        ChaincodeStub stub = ctx.getStub();
        String[] userData = {
                "{ \"belongto\": \"alice\", \"identity\": \"China\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"0112afedcd6d61818beb83505ad9a7b97501b116eec59f6c4b7e07ef0ac7bc6f6d043246f8f2b8b60da8b28a40984c8ba7dca7a3b44ec9671e0a66525d0a0e87bb288e627616382433457d8e3309ad00a35c35d679cddb2632027ad51893f3122b4543c88549184063f1a1980a047d58252095f29891d1cd61427e9eee95105725\" ,\"addTime\" : \"Sun Aug 30 16:17:52 CST 2020\" }",
                "{ \"belongto\": \"bob\", \"identity\": \"US\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"017f224cd143f3feea6483e54c92200b2ba2d1f44f5a74127d6f5c3480067c88d42f7b2b8576ea0305abe79f5d443f485aaeebe1517933221c005e497f9c58cbe802bf622fb94ec38bc937f78c957b1f81816e7a3ae3c4814ab0ee3aab6beb47e896f9a2aabcbcb541ec28d0f01a6a3f53f2938af762bd09321c7589cf10280d5c\" ,\"addTime\" : \"Sun Aug 30 16:17:59 CST 2020\" }"
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

    @Transaction()


    /**
     * @param ctx
     * @param ID 节点id
     * @return 节点所在域的公共管理参数即MPK
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

    /**
     *
     * @param ctx
     * @param IDi
     * @param IDi_1
     * @param message
     * @param signature
     * @return
     */
    @Transaction()
    public String verifyAndUploadPara(final Context ctx, String IDi, String IDi_1, String message, String signature) {

        boolean result = false;

        // 根据ID获取父节点的MPK
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(IDi);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", IDi);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        String publicPara = user.getPublicParameter();
        MasterPublicKey masterPublicKey = MasterPublicKey.fromByteArray(sm9Curve, Hex.decode(publicPara));


        // 根据MPKi, IDi 对 sign 进行验证
        byte[] byteMessage = message.getBytes();
        ResultSignature resSignature = ResultSignature.fromByteArray(sm9Curve, Hex.decode(signature));
        // todo verify(masterPK, identityInfo, message, signature)
        result = sm9.verify(masterPublicKey, IDi, byteMessage, resSignature);

        // 验证成功之后将MPKi_1上链与IDi_1绑定到一起
        if (result == true) {
            userState = stub.getStringState(IDi_1);
            if(userState.isEmpty()) {
                String errorMessage = String.format("Node %s not found", IDi_1);
                System.out.println(errorMessage);
                throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
            }

            String Mi_1 = "";
            try {
                Mi_1 = new String(Base64.getDecoder().decode(message), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            JSONObject object = (JSONObject) JSONObject.parse(Mi_1);
            // todo 从Mi_1 中取出MPKi_1
            String MPKi_1 = object.getString("mpk");

            user = genson.deserialize(userState, User.class);
            User newUser = new User(user.getBelongto(), user.getIdentity(), IDi, 0, 0, MPKi_1, user.getAddTime());
            String newUserState = genson.serialize(newUser);
            stub.putStringState(IDi_1, newUserState);
            return Msg.SUCCESS.toString();
        }
        return Msg.FAIL.toString();
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
