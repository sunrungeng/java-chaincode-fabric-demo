package org.hyperledger.fabric.samples.test;

import com.alibaba.fastjson.JSONObject;
import org.hyperledger.fabric.samples.util.Sha256Util;

public class TestTime {

    public static void main(String[] args) {
//        Genson genson = new Genson();
//        Date date = new Date();
//        String addTime = date.toString();
//        System.out.println(addTime);
//        String[] userData = {
//                "{ \"belongto\": \"alice\", \"identity\": \"China\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"7EB6C877D9D82464F352464BBF9BB8301BAC1A07501215B38647079951D499EEB31926F0524F32A52052A46D734FB9241F2D50CA2736B95469A7CB751CF41391483390F3A8E2F4338F68A861F138E1C4DEEA5F20913E92E5B10B5606DC87B808764F95FAC4A98AA8C7A590732FECE715C340D44A45B84C0AD795245F0EE3A54A\" ,\"addTime\" : \"Sun Aug 30 16:17:52 CST 2020\" }",
//                "{ \"belongto\": \"bob\", \"identity\": \"US\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"28D3CDAB76DFB73E3F74D2FE23AA9752DF4F88D295D3BC073A6F675C0231F16A923A86A86F36F511FC25D432704EBF4CFA278D00639B8D815B4B9A421A79112585886D310ADA30668940DFCF592FC7351BE8CEBCBC4BC2A9BB6806BCBA70EA764AA9E0FE8F1C6785C849D524065B62042545B49698992A38904FF607E8F9DB29\" ,\"addTime\" : \"Sun Aug 30 16:17:59 CST 2020\" }"
//        };
//        for (int i = 0; i < userData.length; i++) {
//            User user = genson.deserialize(userData[i], User.class);
//            String key = user.getIdentity();
//            System.out.println(key);
//        }
        String message = "{\"action\":\"open\",\"accesserIdentity\":\"tsinghua.edu.cn\",\"accessedDevice\":\"watch.tsinghua.edu.cn\",\"time\":\"1599026889379\"}";
        String result = Sha256Util.getSHA256(message);
        System.out.println(result);
//        genInfoJson();

    }
    public static void genInfoJson() {
        JSONObject object = new JSONObject();
        object.put("belongto", "");
        object.put("identity", "tsinghua.edu.cn");
        object.put("zone", "edu.cn");
        object.put("idCount", 0);
        object.put("deviceCount", 0);
        object.put("publicParameter", "0197d9144ba59f7f2f175fc4a7890406c4a5c5f90c235b632e849c7c3410fc1fe43ba31b914b366d0be77b5c3353ae61aa142cb5b033b3af23da0771b9c9c4fd699d5191167b7d71e31b7eff8cefe201d9f82d28b839564b09fa72e6acfe6f51b10cd578c737def1a7b62712a77b579350a6898165d45dba1b7c44ddfe06233646");
        object.put("addTime", "1599029263981");
//        String hashString = Sha256Util.getSHA256(object.toString());
        System.out.println(object.toString());
    }
}
