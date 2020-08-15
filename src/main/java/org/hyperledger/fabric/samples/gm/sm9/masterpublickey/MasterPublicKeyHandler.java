package org.hyperledger.fabric.samples.gm.sm9.masterpublickey;

import org.hyperledger.fabric.samples.gm.sm9.config.ConfigProperties;
import org.hyperledger.fabric.samples.gm.sm9.signal.MessageType;
import org.hyperledger.fabric.samples.gm.sm9.signal.SignalType;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MasterPublicKeyHandler extends Thread {
    private Socket socket;
    private ConfigProperties configProperties;

    public MasterPublicKeyHandler(Socket socket, ConfigProperties configProperties) {
        this.socket = socket;
        this.configProperties = configProperties;
    }

    public void run() {
        BufferedReader bufferedReader = null;
        PrintStream printStream = null;
        try {
            // 获取输入流
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            // 获取消息类型
            String line = bufferedReader.readLine();

            printStream = new PrintStream(socket.getOutputStream());

            if (line.equals(String.valueOf(MessageType.MASTER_KEY_GET_REQUEST))) { // 客户端请求类型为 MASTER_KEY_GET_REQUEST
                // 获取实体标识
                String identity = bufferedReader.readLine();

                // 非顶层KGC，需要从父KGC获取
                if (configProperties.getIsTopKGC().equals("false")) {
                    MasterPublicKeyRequest masterPublicKeyRequest = new MasterPublicKeyRequest(configProperties);
                    String result = masterPublicKeyRequest.getMasterPublicKey(identity);
                    printStream.println(result);
                }

                // 顶层KGC，通过调用智能合约来获取主公钥

                printStream.println(SignalType.MASTER_KEY_GET_NULL);
            } else if (line.equals(String.valueOf(MessageType.MASTER_KEY_SET_REQUEST))) { // 客户端请求类型为 MASTER_KEY_SET_REQUEST
                // 获取实体标识
                String identity = bufferedReader.readLine();
                // 获取实体所在管理域的主公钥
                String masterPublicKey = bufferedReader.readLine();

                // 非顶层KGC，需要委托父KGC更新
                if (configProperties.getIsTopKGC().equals("false")) {
                    MasterPublicKeyRequest masterPublicKeyRequest = new MasterPublicKeyRequest(configProperties);
                    String result = masterPublicKeyRequest.updateMasterPublicKey(identity, masterPublicKey);
                    printStream.println(result);
                }

                // 顶层KGC，通过调用智能合约更新账本
                printStream.println(SignalType.MASTER_KEY_SET_FAIL);
            } else if (line.equals(String.valueOf(MessageType.MASTER_KEY_INVOKE_REQUEST))) { // 客户端请求类型为 MASTER_KEY_INVOKE_REQUEST
                // 获取实体标识
                String identity = bufferedReader.readLine();
                if (configProperties.getIsTopKGC().equals("false")) {
                    MasterPublicKeyRequest masterPublicKeyRequest = new MasterPublicKeyRequest(configProperties);
                    String result = masterPublicKeyRequest.invokeMasterPublicKey(identity);
                    printStream.println(result);
                }

                // 顶层KGC，通过调用智能合约更新账本
                printStream.println(SignalType.MASTER_KEY_INVOKE_FAIL);
            }
            bufferedReader.close();
            printStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
