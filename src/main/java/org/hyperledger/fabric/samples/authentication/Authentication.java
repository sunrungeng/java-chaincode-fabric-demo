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

/**
 * Java implementation of the IOT Authentication Contract
 */
@Contract(
        name = "Authentication",
        info = @Info(
                title = "Authentication contract",
                description = "The Authentication contract",
                version = "1.0",
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
    MasterKeyPair masterKeyPair = kgc.genSignMasterKeyPair();

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
                "{ \"belongto\": \"srg\", \"identity\": \"China\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"7EB6C877D9D82464F352464BBF9BB8301BAC1A07501215B38647079951D499EEB31926F0524F32A52052A46D734FB9241F2D50CA2736B95469A7CB751CF41391483390F3A8E2F4338F68A861F138E1C4DEEA5F20913E92E5B10B5606DC87B808764F95FAC4A98AA8C7A590732FECE715C340D44A45B84C0AD795245F0EE3A54A\" }",
                "{ \"belongto\": \"sch\", \"identity\": \"US\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"28D3CDAB76DFB73E3F74D2FE23AA9752DF4F88D295D3BC073A6F675C0231F16A923A86A86F36F511FC25D432704EBF4CFA278D00639B8D815B4B9A421A79112585886D310ADA30668940DFCF592FC7351BE8CEBCBC4BC2A9BB6806BCBA70EA764AA9E0FE8F1C6785C849D524065B62042545B49698992A38904FF607E8F9DB29\" }"
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
        if(!userState.isEmpty()) {
            String errorMessage = String.format("Node %s already exist", ID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        }
        User user = new User("", ID, "", 0, 0, MPK);
        stub.putStringState(ID, genson.serialize(user));
        return Msg.SUCCESS.toString();
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
        if (userState.isEmpty()) {
            String errorMessage = String.format("Node %s does not exist", IDi);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        userState = stub.getStringState(IDi_1);
        if (!userState.isEmpty()) {
            String errorMessage = String.format("Node %s already exist", IDi);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_ALREADY_EXIST.toString());
        }

        User user = new User("", IDi_1, "", 0, 0, "");
        stub.putStringState(IDi_1, genson.serialize(user));
        return Msg.SUCCESS.toString();
    }

    /**
     * @param ctx
     * @param ID 节点id
     * @return 节点所在域的公共管理参数即MPK
     */
    @Transaction()
    public String queryMPK(final Context ctx, String ID) {
        ChaincodeStub stub = ctx.getStub();
        String userState = stub.getStringState(ID);
        if(userState.isEmpty()) {
            String errorMessage = String.format("Node %s not found", ID);
            System.out.println(errorMessage);
            throw new ChaincodeException(errorMessage, AuthErrors.NODE_KGC_NOT_FOUND.toString());
        }
        User user = genson.deserialize(userState, User.class);
        return user.getPublicParameter();
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
        // todo 此sm9Curve非彼sm9Curve
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
            String MPKi_1 = object.getString("mpk-global");

            user = genson.deserialize(userState, User.class);
            User newUser = new User(user.getBelongto(), user.getIdentity(), IDi, 0, 0, MPKi_1);
            String newUserState = genson.serialize(newUser);
            stub.putStringState(IDi_1, newUserState);
            return Msg.SUCCESS.toString();
        }
        return Msg.FAIL.toString();
    }

//    @Transaction()
//    public String signature_key_generate(final Context ctx, final String key, String identityInfo) {
//        PrivateKey signatureKey;
//        String hexStrSignatureKey = null;
//        ChaincodeStub stub = ctx.getStub();
//        String authState = stub.getStringState(key);
//        if (!authState.isEmpty()) {
//            String err = String.format("Auth %s already exists", key);
//            System.out.println(err);
//            throw new ChaincodeException(err, "Auth %s already exists");
//        }
//
//        try {
//            signatureKey = kgc.genPrivateKey(masterKeyPair.getPrivateKey(), identityInfo, PrivateKeyType.KEY_SIGN);
//            hexStrSignatureKey = Hex.encodeToString(signatureKey.toByteArray());
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//        authState = genson.serialize(hexStrSignatureKey);
//        stub.putStringState(key, authState);
//
//        return hexStrSignatureKey;
//    }

}
