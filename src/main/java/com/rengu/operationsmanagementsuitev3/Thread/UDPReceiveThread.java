package com.rengu.operationsmanagementsuitev3.Thread;

import com.rengu.operationsmanagementsuitev3.Entity.HeartbeatEntity;
import com.rengu.operationsmanagementsuitev3.Service.DeviceService;
import com.rengu.operationsmanagementsuitev3.Utils.ApplicationConfig;
import com.rengu.operationsmanagementsuitev3.Utils.FormatUtils;
import com.rengu.operationsmanagementsuitev3.Utils.JsonUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.*;

/**
 * @program: OperationsManagementSuiteV3
 * @author: hanchangming
 * @create: 2018-09-04 17:17
 **/

@Slf4j
@Component
public class UDPReceiveThread {

    private final SimpMessagingTemplate simpMessagingTemplate;

    @Autowired
    public UDPReceiveThread(SimpMessagingTemplate simpMessagingTemplate) {

        this.simpMessagingTemplate = simpMessagingTemplate;
    }

    @Async
    public void UDPMessageReceiver() throws IOException {
        log.info("OMS服务器-启动客户端UDP报文监听线程，监听端口：" + ApplicationConfig.UDP_RECEIVE_PORT);
        DatagramSocket datagramSocket = new DatagramSocket(ApplicationConfig.UDP_RECEIVE_PORT);
        DatagramPacket datagramPacket = new DatagramPacket(new byte[512], 512);
        while (true) {
            datagramSocket.receive(datagramPacket);
            // 解析心跳报文信息
            byte[] bytes = datagramPacket.getData();
            int pointer = 0;
            String cpuTag = "";
            long cpuClock = 0;
            int cpuUtilization = 0;
            int ramTotalSize = 0;
            int freeRAMSize = 0;
            double upLoadSpeed = 0.0;
            double downLoadSpeed = 0.0;
            int OSType = 0;
            String OSName = "";
            try {
                String codeType = new String(bytes, pointer, 4).trim();
                pointer = pointer + 4;
                OSType = bytes[pointer];
                pointer = pointer + 1;
                OSName = new String(bytes, pointer, 16).trim();
                pointer = pointer + 16;
                cpuTag = new String(bytes, pointer, 64).trim();
                pointer = pointer + 64;
                cpuClock = Long.parseLong(new String(bytes, pointer, 6).trim());
                pointer = pointer + 6;
                cpuUtilization = Integer.parseInt(new String(bytes, pointer, 4).trim());
                pointer = pointer + 4;
                ramTotalSize = Integer.parseInt(new String(bytes, pointer, 6).trim());
                pointer = pointer + 6;
                freeRAMSize = Integer.parseInt(new String(bytes, pointer, 6).trim());
                pointer = pointer + 6;
                upLoadSpeed = Double.parseDouble(new String(bytes, pointer, 8).trim());
                pointer = pointer + 8;
                downLoadSpeed = Double.parseDouble(new String(bytes, pointer, 8).trim());
            } catch (Exception e) {
                log.info("心跳格式解析异常:" + e.getMessage());
                e.printStackTrace();
            }
            HeartbeatEntity heartbeatEntity = new HeartbeatEntity();
            heartbeatEntity.setHostAddress(datagramPacket.getAddress().getHostAddress());
            heartbeatEntity.setCpuTag(cpuTag);
            heartbeatEntity.setCpuClock(cpuClock);
            heartbeatEntity.setCpuUtilization(cpuUtilization);
            heartbeatEntity.setRamTotalSize(ramTotalSize);
            heartbeatEntity.setRamFreeSize(freeRAMSize);
            heartbeatEntity.setUpLoadSpeed(upLoadSpeed);
            heartbeatEntity.setDownLoadSpeed(downLoadSpeed);
            heartbeatEntity.setOSType(OSType);
            heartbeatEntity.setOSName(OSName);
            simpMessagingTemplate.convertAndSend("/deviceInfo/" + heartbeatEntity.getHostAddress(), JsonUtils.toJson(heartbeatEntity));
            if (heartbeatEntity.getCpuUtilization() > 100 || heartbeatEntity.getCpuClock() > 6000) {
                log.info(heartbeatEntity.toString());
            }
            if (!DeviceService.ONLINE_HOST_ADRESS.containsKey(heartbeatEntity.getHostAddress())) {
                log.info(heartbeatEntity.getHostAddress() + "----->建立服务器连接。");
            }
            DeviceService.ONLINE_HOST_ADRESS.put(heartbeatEntity.getHostAddress(), heartbeatEntity);
            simpMessagingTemplate.convertAndSend("/onlineDevice", JsonUtils.toJson(DeviceService.ONLINE_HOST_ADRESS));
        }
    }

    //SRC_CSGK = 1,       1、一体化运维=测试管控
    //SRC_SIMU = 2,       2、仿真工具
    //SRC_TXJM = 3,       3、体系建模软件
    //SRC_SJCJ = 4,       4、数据采集软件
    //SRC_GZZR = 5,       5、故障注入
    //SRC_ZNCS = 6,       6、智能测试
    //SRC_DZSP = 7,       7、电子沙盘
    //SRC_XX2  = 8        8、XX2实装软件

    //PT_JZGK  = 1,       1、集中管控
    //PT_TSXX             2、态势信息

//    @Async
//    //通过udp发送CMDValue状态
//    //发送端
//    public void UDPInitializationState(String fileName, String status) throws IOException {
//        log.info("OMS服务器-启动客户端CMDValue--UDP报文监听线程，监听端口：" + ApplicationConfig.UDP_SEND_PORT);
//        //Byte[] bytes = new Byte[280];
//        DatagramSocket ds = new DatagramSocket();
//        String srcFlay1 = "1";
//        String strFlay = FormatUtils.StrToBinstr(srcFlay1);
//        int var = 0;
//        String pachIndex1 = String.valueOf(var);
//        String pachIndex = FormatUtils.StrToBinstr(pachIndex1);
//
//        String cmdValue1 = status;
//        String cmdValue = FormatUtils.StrToBinstr(cmdValue1);
//        String cmdType1 = "1";
//        String cmdType = FormatUtils.StrToBinstr(cmdType1);
//        //String fName = fileName;
//        String fName1 = "2020-07-04 22-38-29-4个目标1个无人机.xml";
//        String fName = FormatUtils.StrToBinstr(fName1);
//        String msgLength03 = "msgLength";
//        String msgLength01 = FormatUtils.StrToBinstr(msgLength03);
//
//
//        int msgLength02 = FormatUtils.getLength(strFlay + pachIndex + cmdValue + cmdType + fName + msgLength01);
//        String msgLength = String.valueOf(msgLength02);
//
//        String messageInformation = msgLength + strFlay + pachIndex + cmdValue + cmdType + fName;
//        //String str = FormatUtils.StrToBinstr(messageInformation);
//        int b = messageInformation.getBytes("UTF-8").length;
//        byte[] bys = FormatUtils.getString(messageInformation, b).getBytes();
//        int length = bys.length;
//        // 创建IP地址对象
//        InetAddress address = InetAddress.getByName(ApplicationConfig.SERVER_CAST_ADDRESS);
//        //把数据进行打包
//        DatagramPacket dp = new DatagramPacket(bys, length, address,ApplicationConfig.UDP_SEND_PORT);
//        ds.send(dp);
//        ds.close();
//    }

    @Async
    //通过udp发送CMDValue状态
    //发送端
    public void UDPInitializationState(String fileName, int status) throws IOException {
        log.info("OMS服务器-启动客户端CMDValue--UDP报文监听线程，监听端口：" + ApplicationConfig.UDP_SEND_PORT);
        DatagramSocket ds = new DatagramSocket();
        int srcFlag = 1;
        int packIndex = 1;
        int packType = 1;
        int cmdValue = status;
        int cmdType = 1;
        String fileNames = fileName;
        byte[] b6 = new String(fileNames).getBytes("utf-8");
        byte[] b1 = FormatUtils.intToByteArray(srcFlag);
        byte[] b2 = FormatUtils.intToByteArray(packIndex);
        byte[] b3 = FormatUtils.intToByteArray(packType);
        byte[] b4 = FormatUtils.intToByteArray(cmdValue);
        byte[] b5 = FormatUtils.intToByteArray(cmdType);
        byte[] endBys = combineByte(b1, b2, b3, b4, b5, b6);
        byte[] slen = FormatUtils.intToByteArray(endBys.length);
        byte[] endBys1 = combineByte(slen, endBys);
        InetAddress address = InetAddress.getByName(ApplicationConfig.SERVER_CAST_ADDRESS);
//        //把数据进行打包
        DatagramPacket dp = new DatagramPacket(endBys1, endBys1.length, address, ApplicationConfig.UDP_SEND_PORT);
        ds.send(dp);
        ds.close();
    }

    /**
     * 整合
     *
     * @param bytes
     * @return
     */
    public static byte[] combineByte(byte[]... bytes) {
        int length = 0;
        for (byte[] b : bytes) {
            length += b.length;
        }
        byte[] allByte = new byte[length];
        int positon = 0;
        for (byte[] b : bytes) {
            System.arraycopy(b, 0, allByte, positon, b.length);
            positon += b.length;
        }
        return allByte;
    }

    @Async
    //通过udp发送CMDValue状态
    //发送端
    public void UDPState(int status) throws IOException {
        log.info("OMS服务器-启动客户端CMDValue--UDP报文监听线程，监听端口：" + ApplicationConfig.UDP_SEND_PORT);
        DatagramSocket ds = new DatagramSocket();
        int srcFlag = 1;
        int packIndex = 1;
        int packType = 1;
        int cmdValue = status;
        int cmdType = 1;
        byte[] b1 = FormatUtils.intToByteArray(srcFlag);
        byte[] b2 = FormatUtils.intToByteArray(packIndex);
        byte[] b3 = FormatUtils.intToByteArray(packType);
        byte[] b4 = FormatUtils.intToByteArray(cmdValue);
        byte[] b5 = FormatUtils.intToByteArray(cmdType);
        byte[] endBys = combineByte(b1, b2, b3, b4, b5);
        byte[] slen = FormatUtils.intToByteArray(endBys.length);
        byte[] endBys1 = combineByte(slen, endBys);
        InetAddress address = InetAddress.getByName(ApplicationConfig.SERVER_CAST_ADDRESS);
//        //把数据进行打包
        DatagramPacket dp = new DatagramPacket(endBys1, endBys1.length, address, ApplicationConfig.UDP_SEND_PORT);
        ds.send(dp);
        ds.close();
    }

    @Async
    //TODO:TCP报文接受进程
    public void TCPMessageValuse() {
        try {
            log.info("OMS服务器-启动客户端CMDValue--TCP报文监听线程，监听端口：" + ApplicationConfig.TCP_RECEIVES_PORT);

            // 1、创建客户端对象
            ServerSocket serverSocket = new ServerSocket(ApplicationConfig.TCP_RECEIVES_PORT);
            while (true) {
                Socket socket = serverSocket.accept();
                // 2、获取连接过来的客户端对象
                String ip = socket.getInetAddress().getHostAddress();
                System.out.println("获取客户端ip:" + ip);
                socketHandler(socket);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Async
    public void socketHandler(Socket socket) throws IOException {
        try {
            // 3、通过 Socket 对象获取输入流，读取客户端发来的数据
            InputStream inputStream = socket.getInputStream();
            StringBuffer acceptMsg = new StringBuffer();
            int MsgLong = 0;    //接收的总长度
            int lenght = 0;     //每次容器读取时的长度
            byte[] bytes = new byte[1024];
            while ((lenght = inputStream.read(bytes)) != -1) {//数据一直在读取，知道没有为止
                acceptMsg.append(new String(bytes, 0, lenght, "ISO-8859-1"));
                FormatUtils.byteArrToInt(bytes);
                MsgLong += lenght;
                if (lenght < 1024) {//如果读的长度小于1024，说明是最后一次读,后面已经没有数据，跳出循环
                    break;
                }
            }
            // 处理客户端数据
            System.out.println("客户端发过来的内容长度:" + MsgLong);
            System.out.println("客户端发过来的内容:" + acceptMsg.toString());
            // 向客户端回复信息
            DataOutputStream out = new DataOutputStream(socket.getOutputStream());
            // 发送键盘输入的一行
            String s = "server send msg to client";
            System.out.print("服务端返回数据:\t" + s);
            out.write(s.getBytes());

            out.close();
            inputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            socket.shutdownOutput();
            socket.close();
        }
    }

//    private void bytesHandler(byte[] bytes) {
//        try {
//            int pointer = 0;
//            String srcFlay = new String(bytes, 0, 10).trim();
//            //socket.getInputStream().read(bytes,0,10);
//            //System.out.println("获取信息:"+socket.getInputStream().read(bytes,0,10));
//            System.out.println("打印出：" + srcFlay);
//            pointer = pointer + 10;
//            String packIndex = new String(bytes, pointer, 10).trim();
//            System.out.println("打印出1：" + packIndex);
//            pointer = pointer + 10;
//            String packType = new String(bytes, pointer, 410).trim();
//            System.out.println("打印出2：" + packType);
//        } catch (NumberFormatException e) {
//            e.printStackTrace();
//        }
//    }
}

////接收端
//class UdpAccept implements Runnable {
//    public String path = "F:" + File.separator + "test" + File.separator + "jilu.txt";
//
//    @Override
//    public void run() {
//        DatagramSocket ds = null;
//        try {
//            ds = new DatagramSocket(5555);
//        } catch (SocketException e) {
//            e.printStackTrace();
//        }
//        while (true) {
//            try {
//                byte[] b = new byte[1024];
//                DatagramPacket dp = new DatagramPacket(b, b.length);
//                ds.receive(dp);
//                String str = new String(dp.getData(), 0, dp.getLength(), "GBK");
//                SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//                String date = simpleDateFormat.format(new Date());
//                String content = "\t" + str;
//                System.out.println("\n" + date);
//                System.out.println(content);
//                write(content, date);
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    //将对方发送内容写入文本
//    public void write(String content, String date) throws Exception {
//        FileOutputStream fileOutputStream = new FileOutputStream(this.path, true);
//        date = "\r\n" + date + "\r\n";
//        StringBuffer stringBuffer = new StringBuffer(date + content);
//        String re = stringBuffer.toString();
//        byte[] result = re.getBytes();
//        fileOutputStream.write(result);
//        fileOutputStream.close();
//    }
//}
//
////初始化状态
//@Async
//class UDPMessageReceivers {
//    UDPMessageReceivers serverSocketTest = new UDPMessageReceivers();
//    ExecutorService executor = Executors.newFixedThreadPool(3);
//
//    public UDPMessageReceivers() {
//        //udp广播
//        System.out.println("----server-run-----");
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(2000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        serverSocketTest.BroadcastIP("");
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//        //tcp点对点连接
//        executor.execute(new Runnable() {
//            @Override
//            public void run() {
//                while (true) {
//                    try {
//                        Thread.sleep(1000);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    try {
//                        serverSocketTest.peerToPeerService();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                }
//            }
//        });
//
////        new ServerSocketTest().lanchApp();
//        try {
//            executor.shutdown();
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//
//    //tcp服务
//    private void peerToPeerService() throws IOException {
//        String clientSentence;
//        String capitalizedSentence;
//        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//        //创建服务器端 Socket 并指明端口号
//        ServerSocket welcomeSocket = new ServerSocket(6789);
//        while (true) {
//            //接收客户端连接
//            Socket connectionSocket = welcomeSocket.accept();
//            BufferedReader inFromClient = new BufferedReader(new InputStreamReader(connectionSocket.getInputStream()));
//            //获取客户端传入的字符串
//            clientSentence = inFromClient.readLine();
//            if (clientSentence != null)
//                System.out.println(df.format(new Date()) + " from " + connectionSocket.getInetAddress() + ": " + clientSentence);
//            InetAddress address = null;
//            if (connectionSocket.getInetAddress() != null) {
//                address = connectionSocket.getInetAddress();
//            }
////                setClientData(address, connectionSocket.getPort());
//
//        }
//    }
//
//    //组播服务
//    private void BroadcastIP(String otherMsg) throws Exception {
//        //srcFlag packType
//        DatagramSocket dgSocket = new DatagramSocket();
//        //String host = "224.0.0.1";
//        InetAddress group = InetAddress.getByName(ApplicationConfig.SERVER_CAST_ADDRESS);
//        MulticastSocket multicastSocket = new MulticastSocket();
//        multicastSocket.joinGroup(group);
//        byte b[] = ("srcFlag(type--int):1-8,packType(type--int):1-3\n" + otherMsg).getBytes();
//        DatagramPacket dgPacket = new DatagramPacket(b, b.length, group, ApplicationConfig.UDP_SEND_PORT);
//        dgSocket.send(dgPacket);
//        dgSocket.close();
//        System.out.println("组播中---send message is ok");
//    }
//
//}
//
//    //保存客户端信息到set集合
//    private void setClientData(InetAddress IPAddress, int port) {
//        System.out.println(IPAddress);
//        String s1 = IPAddress.getHostAddress() + ":" + port;
//    }
//}
