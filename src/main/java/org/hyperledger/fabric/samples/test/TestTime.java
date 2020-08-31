package org.hyperledger.fabric.samples.test;

import com.owlike.genson.Genson;
import org.hyperledger.fabric.samples.authentication.User;

import java.util.Date;

public class TestTime {

    public static void main(String[] args) {
        Genson genson = new Genson();
        Date date = new Date();
        String addTime = date.toString();
        System.out.println(addTime);
        String[] userData = {
                "{ \"belongto\": \"alice\", \"identity\": \"China\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"7EB6C877D9D82464F352464BBF9BB8301BAC1A07501215B38647079951D499EEB31926F0524F32A52052A46D734FB9241F2D50CA2736B95469A7CB751CF41391483390F3A8E2F4338F68A861F138E1C4DEEA5F20913E92E5B10B5606DC87B808764F95FAC4A98AA8C7A590732FECE715C340D44A45B84C0AD795245F0EE3A54A\" ,\"addTime\" : \"Sun Aug 30 16:17:52 CST 2020\" }",
                "{ \"belongto\": \"bob\", \"identity\": \"US\", \"zone\": \"\", \"idCount\": \"0\", \"deviceCount\":\"0\", \"publicParameter\":\"28D3CDAB76DFB73E3F74D2FE23AA9752DF4F88D295D3BC073A6F675C0231F16A923A86A86F36F511FC25D432704EBF4CFA278D00639B8D815B4B9A421A79112585886D310ADA30668940DFCF592FC7351BE8CEBCBC4BC2A9BB6806BCBA70EA764AA9E0FE8F1C6785C849D524065B62042545B49698992A38904FF607E8F9DB29\" ,\"addTime\" : \"Sun Aug 30 16:17:59 CST 2020\" }"
        };
        for (int i = 0; i < userData.length; i++) {
            User user = genson.deserialize(userData[i], User.class);
            String key = user.getIdentity();
            System.out.println(key);
        }
    }
}
