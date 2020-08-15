package org.hyperledger.fabric.samples.test.masterpublickey;

import org.hyperledger.fabric.samples.gm.sm9.config.ConfigProperties;
import org.hyperledger.fabric.samples.gm.sm9.masterpublickey.MasterPublicKeyHandler;
import org.hyperledger.fabric.samples.gm.sm9.masterpublickey.MasterPublicKeyRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class KGCLevel1Set {
    public static void main(String[] args) {
        ConfigProperties configProperties = new ConfigProperties("config/kgcLevel1.properties");
        String here = "server";

        // 客户端KGC
        if (here.equals("client")) {
            MasterPublicKeyRequest masterPublicKeyRequest = new MasterPublicKeyRequest(configProperties);
            masterPublicKeyRequest.getMasterPublicKey("stone");
        } else {
            ServerSocket server = null;
            try {
                server = new ServerSocket(Integer.valueOf(configProperties.getPortThisKGC()));

                while (true) {
                    Socket socket = server.accept();
                    System.out.println("KGC1 is not top level KGC, delegate KGC0 to update master public key for \"stone\" ...");
                    MasterPublicKeyHandler masterPublicKeyHandler = new MasterPublicKeyHandler(socket, configProperties);
                    masterPublicKeyHandler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
