package com.rengu.operationsmanagementsuitev3.Thread;

import java.io.DataOutputStream;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPServer3 {
    public static final int PORT = 6006;//监听的端口号

    public static void main(String[] args) {
        System.out.println("TCP服务器启动:\n");
        TCPServer3 server = new TCPServer3();
        server.init();
    }

    public void init() {
        try {
            ServerSocket serverSocket = new ServerSocket(PORT);
            while (true) {
                Socket client = serverSocket.accept();
                System.out.println("接到新连接：" + client.getInetAddress() + "-" + client.getPort());
                // 用线程处理
                new HandlerThread(client);
            }
        } catch (Exception e) {
            System.out.println("服务器异常: " + e.getMessage());
        }
    }

    private class HandlerThread implements Runnable {
        private Socket socket;
        public HandlerThread(Socket client) {
            socket = client;
            new Thread(this).start();
        }

        public void run() {
            try {
                // 读取客户端数据
                InputStream input = socket.getInputStream();

                StringBuffer acceptMsg = new StringBuffer();
                int MsgLong = 0;//接收数据总长度
                int len = 0;  //每次容器读时的长度
                byte[] b = new byte[1024]; //容器，存放数据

                while ((len = input.read(b)) != -1) {//一直读，读到没数据为止
                    acceptMsg.append(new String(b, 0, len, "ISO-8859-1"));
                    MsgLong += len;
                    if (len < 1024) {//如果读的长度小于1024，说明是最后一次读,后面已经没有数据，跳出循环
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
                System.out.print("服务端返回数据:\t"+s);
                out.write(s.getBytes());

                out.close();
                input.close();
            } catch (Exception e) {
                System.out.println("服务器 run 异常: " + e.getMessage());
            } finally {
                if (socket != null) {
                    try {
                        socket.close();
                    } catch (Exception e) {
                        socket = null;
                        System.out.println("服务端 finally 异常:" + e.getMessage());
                    }
                }
            }
        }
    }

}
