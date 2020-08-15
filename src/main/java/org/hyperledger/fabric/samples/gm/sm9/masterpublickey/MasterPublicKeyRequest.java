package org.hyperledger.fabric.samples.gm.sm9.masterpublickey;


import org.hyperledger.fabric.samples.gm.sm9.config.ConfigProperties;
import org.hyperledger.fabric.samples.gm.sm9.signal.MessageType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MasterPublicKeyRequest {
    private ConfigProperties configProperties;

    public MasterPublicKeyRequest(ConfigProperties configProperties) {
        this.configProperties = configProperties;
    }

    public String getMasterPublicKey(String identity) {
        String answer = null;
        try {
            Socket client = new Socket(configProperties.getIpParentKGC(), Integer.valueOf(configProperties.getPortParentKGC()));
            // 客户端读取超时设置：10s
            client.setSoTimeout(10000);
            PrintStream printStream = new PrintStream(client.getOutputStream());
            // 发送消息类型，发送出去之后是字符串类型："MASTER_KEY_GET_REQUEST"（并不是数字）
            printStream.println(MessageType.MASTER_KEY_GET_REQUEST);
            // 实体标识
            printStream.println(identity);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // 读取父KGC传送的主公钥
            answer = bufferedReader.readLine();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return answer;
    }

    public String updateMasterPublicKey(String identity, String masterPublicKey) {
        String answer = null;
        try {
            Socket client = new Socket(configProperties.getIpParentKGC(), Integer.valueOf(configProperties.getPortParentKGC()));
            // 客户端读取超时设置：10s
            client.setSoTimeout(10000);
            PrintStream printStream = new PrintStream(client.getOutputStream());
            // 发送消息类型
            printStream.println(MessageType.MASTER_KEY_SET_REQUEST);
            // 发送实体标识
            printStream.println(identity);
            // 发送实体所在管理域的主公钥
            printStream.println(masterPublicKey);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // 读取更新操作返回结果
            answer = bufferedReader.readLine();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }

    public String invokeMasterPublicKey(String identity) {
        String answer = null;
        try {
            Socket client = new Socket(configProperties.getIpParentKGC(), Integer.valueOf(configProperties.getPortParentKGC()));
            // 客户端读取超时设置：10s
            client.setSoTimeout(10000);
            PrintStream printStream = new PrintStream(client.getOutputStream());
            // 发送消息类型
            printStream.println(MessageType.MASTER_KEY_INVOKE_REQUEST);
            // 发送实体标识
            printStream.println(identity);

            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(client.getInputStream()));
            // 读取更新操作返回结果
            answer = bufferedReader.readLine();

            client.close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        return answer;
    }
}
