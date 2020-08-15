package org.hyperledger.fabric.samples.gm.sm9.config;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;

//import java.net.URLDecoder;

public class ConfigProperties {
    private String ipParentKGC; // 父KGC的ip；
    private String portParentKGC; // 父KGC的端口；
    private String ipThisKGC; // 当前KGC的ip；
    private String portThisKGC; // 当前KGC的端口
    private String isTopKGC; // 指明当前KGC是否是顶层KGC

    public ConfigProperties(String configPath) {
        Properties properties = new Properties();
        try {
            // 实现动态加载配置文件（不受缓存机制影响）
//            String path = Thread.currentThread().getContextClassLoader().getResource(configPath).getPath();
//            path = URLDecoder.decode(path, "UTF-8");
//            FileInputStream fileInputStream = new FileInputStream(path);
            // 加载配置文件
            FileInputStream fileInputStream = new FileInputStream(configPath);
            properties.load(fileInputStream);

            // 读取配置信息
            ipParentKGC = properties.getProperty("ipParentKGC");
            portParentKGC = properties.getProperty("portParentKGC");
            ipThisKGC = properties.getProperty("ipThisKGC");
            portThisKGC = properties.getProperty("portThisKGC");
            isTopKGC = properties.getProperty("isTopKGC");

            // 读取完配置后关闭文件
            fileInputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String getIpParentKGC() {
        return ipParentKGC;
    }

    public void setIpParentKGC(String ipParentKGC) {
        this.ipParentKGC = ipParentKGC;
    }

    public String getPortParentKGC() {
        return portParentKGC;
    }

    public void setPortParentKGC(String portParentKGC) {
        this.portParentKGC = portParentKGC;
    }

    public String getIpThisKGC() {
        return ipThisKGC;
    }

    public void setIpThisKGC(String ipThisKGC) {
        this.ipThisKGC = ipThisKGC;
    }

    public String getPortThisKGC() {
        return portThisKGC;
    }

    public void setPortThisKGC(String portThisKGC) {
        this.portThisKGC = portThisKGC;
    }

    public String getIsTopKGC() {
        return isTopKGC;
    }

    public void setIsTopKGC(String isTopKGC) {
        this.isTopKGC = isTopKGC;
    }
}
