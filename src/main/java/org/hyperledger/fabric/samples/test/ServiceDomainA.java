package org.hyperledger.fabric.samples.test;

import org.hyperledger.fabric.samples.gm.sm9.*;
import org.hyperledger.fabric.samples.util.Hex;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.ServerSocket;
import java.net.Socket;

public class ServiceDomainA {
    public static void main(String[] args) throws Exception{
        ServerSocket serverSocket = new ServerSocket(63363);

        System.out.println("服务器正在等在认证请求……");

        while (true) {
            Socket socket = serverSocket.accept();
            ServerThread serverThread = new ServerThread(socket);
            serverThread.start();
        }
    }
}

class ServerThread extends Thread{
    Socket socket;
    public ServerThread(Socket socket) {
        this.socket = socket;
    }

    public void run() {

        SM9Curve sm9Curve = null;
        try {
            sm9Curve = new SM9Curve();
        } catch (Exception e) {
            e.printStackTrace();
        }
        assert (sm9Curve != null);
        SM9 sm9 = new SM9(sm9Curve);

        BufferedReader bufferedReader = null;

        try {
            bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            String line = bufferedReader.readLine();
            if (line.equals("REQUEST")) {
                System.out.println("接收到认证请求消息：" + "REQUEST");

                String receivedMasterPublicKey = bufferedReader.readLine();
                System.out.print("接收到的主公钥参数：");
                System.out.println(receivedMasterPublicKey);

                String receivedSignature = bufferedReader.readLine();
                System.out.print("接收到的消息签名：");
                System.out.println(receivedSignature);

                String receivedVerificationKey = bufferedReader.readLine();
                System.out.print("接收到的签名验证公钥：");
                System.out.println(receivedVerificationKey);

                String receivedMessage = bufferedReader.readLine();
                System.out.print("接收到的认证消息：");
                System.out.println(receivedMessage);
                MasterPublicKey masterPublicKey = MasterPublicKey.fromByteArray(sm9Curve, Hex.decode(receivedMasterPublicKey));
                ResultSignature signature = ResultSignature.fromByteArray(sm9Curve, Hex.decode(receivedSignature));
                boolean ok = sm9.verify(masterPublicKey, receivedVerificationKey, receivedMessage.getBytes(), signature);
                if (ok) {
                    System.out.println("认证结果：认证成功！");
                    System.out.print("设备身份信息：");
                    System.out.println(receivedVerificationKey);

                    PrintStream printStream = new PrintStream(socket.getOutputStream());
                    printStream.println("SUCCESS");

                    System.out.println("回传认证结果：" + "SUCCESS");
                } else {
                    System.out.println("认证结果：认证失败！");
                }
            } else {
                System.out.println(line);
                System.out.println("无效信息!!!");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
