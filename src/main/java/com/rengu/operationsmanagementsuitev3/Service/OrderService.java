package com.rengu.operationsmanagementsuitev3.Service;

import com.rengu.operationsmanagementsuitev3.Entity.FileEntity;
import com.rengu.operationsmanagementsuitev3.Entity.OrderEntity;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import lombok.extern.slf4j.Slf4j;
import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.*;
import java.nio.ByteBuffer;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-08-30 15:23
 **/

@Slf4j
@Service
public class OrderService {

    public static final String DEPLOY_DESIGN_SCAN = "S102";
    public static final String DEPLOY_DESIGN_SCAN_WITH_EXTENSIONS = "S103";
    public static final String PROCESS_SCAN_TAG = "S105";
    public static final String DISK_SCAN_TAG = "S106";
    // 客户端返回报问表示
    public static final String DEPLOY_DESIGN_SCAN_RESULT_TAG = "C102";
    public static final String PROCESS_SCAN_RESULT_TAG = "C105";
    public static final String DISK_SCAN_RESULT_TAG = "C106";

    public void sendProcessScanOrderByUDP(OrderEntity orderEntity) throws IOException {
        String tag = FormatUtils.getString(orderEntity.getTag(), 4);
        String type = FormatUtils.getString("", 1);
        String uuid = FormatUtils.getString(orderEntity.getId(), 37);
        sandMessageByUDP(orderEntity.getTargetDevice().getHostAddress(), tag + type + uuid);
    }

    public void sendDiskScanOrderByUDP(OrderEntity orderEntity) throws IOException {
        String tag = FormatUtils.getString(orderEntity.getTag(), 4);
        String type = FormatUtils.getString("", 1);
        String uuid = FormatUtils.getString(orderEntity.getId(), 37);
        sandMessageByUDP(orderEntity.getTargetDevice().getHostAddress(), tag + type + uuid);
    }

    //TODO:通过UDP发送部署设计扫描顺序
    public void sendDeployDesignScanOrderByUDP(OrderEntity orderEntity) throws IOException {
        String tag = FormatUtils.getString(orderEntity.getTag(), 4);
        String uuid = FormatUtils.getString(orderEntity.getId(), 37);
        String deploymentDesignNodeId = FormatUtils.getString(orderEntity.getDeploymentDesignNodeEntity().getId(), 37);
        String deploymentDesignDetailId = FormatUtils.getString(orderEntity.getDeploymentDesignDetailEntity().getId(), 37);
        String targetPath = FormatUtils.getString(orderEntity.getTargetPath(), 256);

        if (StringUtils.isEmpty(orderEntity.getExtension())) {
            sandMessageByUDP(orderEntity.getTargetDevice().getHostAddress(), tag + uuid + deploymentDesignNodeId + deploymentDesignDetailId + targetPath);
        } else {
            String extension = FormatUtils.getString(orderEntity.getExtension(), 128);
            sandMessageByUDP(orderEntity.getTargetDevice().getHostAddress(), tag + uuid + deploymentDesignNodeId + deploymentDesignDetailId + extension + targetPath);
        }
    }

    /**
     * 下面是在 Java 中使用 UDP 协议发送数据的步骤。
     * 使用 DatagramSocket() 创建一个数据包套接字。
     * 使用 DatagramPacket() 创建要发送的数据包。
     * 使用 DatagramSocket 类的 send() 方法发送数据包。
     *
     * 接收 UDP 数据包的步骤如下：
     * 使用 DatagramSocket 创建数据包套接字，并将其绑定到指定的端口。
     * 使用 DatagramPacket 创建字节数组来接收数据包。
     * 使用 DatagramPacket 类的 receive() 方法接收 UDP 包。
     *
     * @param hostAdress
     * @param message
     * @throws IOException
     */
    //todo :通过UDP发送
    private void sandMessageByUDP(String hostAdress, String message) throws IOException {
        //TODO:使用DatagramSocket创建一个数据包套接字
        DatagramSocket datagramSocket = new DatagramSocket();
        //TODO: 匹配某个IP地址来实例化一个InetAddress
        InetAddress inetAddress = InetAddress.getByName(hostAdress);
        //TODO :此类实现 IP 套接字地址（IP 地址 + 端口号
        SocketAddress socketAddress = new InetSocketAddress(inetAddress, ApplicationConfig.UDP_SEND_PORT);
        //TODO：使用 DatagramPacket() 创建要发送的数据包
        //TODO： DatagramPacket(byte[] buf, int offset, int length, SocketAddress address)
        //TODO： 构造数据报包，用来将长度为 length 偏移量为 offset 的包发送到指定主机上的指定端口号。
        DatagramPacket datagramPacket = new DatagramPacket(message.getBytes(), message.length(), socketAddress);
        //TODO：使用 DatagramSocket 类的 send() 方法发送数据包
        datagramSocket.send(datagramPacket);
        datagramSocket.close();
    }
}
