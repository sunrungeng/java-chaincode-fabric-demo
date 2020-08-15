package org.hyperledger.fabric.samples.test.masterpublickey;

import org.hyperledger.fabric.samples.gm.sm9.config.ConfigProperties;
import org.hyperledger.fabric.samples.gm.sm9.masterpublickey.MasterPublicKeyHandler;
import org.hyperledger.fabric.samples.gm.sm9.masterpublickey.MasterPublicKeyRequest;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class KGCLevel2Set {
    public static void main(String[] args) {
        ConfigProperties configProperties = new ConfigProperties("config/kgcLevel2.properties");
        String here = "client";

        // 客户端KGC
        if (here.equals("client")) {
            System.out.println("KGC2 is not top level KGC,  delegate KGC1 to update master public key for \"stone\" ...");
            MasterPublicKeyRequest masterPublicKeyRequest = new MasterPublicKeyRequest(configProperties);
            String identity = "stone";
            String masterPublicKey = "stone's master public key";
            System.out.println(masterPublicKeyRequest.updateMasterPublicKey(identity, masterPublicKey));
        } else {
            ServerSocket server = null;
            try {
                server = new ServerSocket(Integer.valueOf(configProperties.getPortThisKGC()));

                while (true) {
                    Socket socket = server.accept();
                    MasterPublicKeyHandler masterPublicKeyHandler = new MasterPublicKeyHandler(socket, configProperties);
                    masterPublicKeyHandler.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
