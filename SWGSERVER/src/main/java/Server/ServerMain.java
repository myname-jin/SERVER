/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Server;

/**
 *
 * @author adsd3
 */
import java.io.*;
import java.net.*;
import java.util.*;
import Server.SessionManager;
import Server.ClientHandler;

public class ServerMain {

    public static final int PORT = 5000;

    public static void main(String[] args) {
        try {
            // ✅ 기존: 공인 IP 출력
            try (InputStream is = new URL("https://api.ipify.org").openStream();
                 BufferedReader br = new BufferedReader(new InputStreamReader(is))) {
                System.out.println("외부 접속용 공인 IP: " + br.readLine());
            } catch (IOException e) {
                System.out.println("공인 IP를 가져올 수 없습니다.");
            }

            // ✅ 기존: 내부 IP 목록 출력
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            while (interfaces.hasMoreElements()) {
                NetworkInterface iface = interfaces.nextElement();
                if (!iface.isUp() || iface.isLoopback()) continue;
                for (InterfaceAddress addr : iface.getInterfaceAddresses()) {
                    InetAddress inet = addr.getAddress();
                    if (inet instanceof Inet4Address) {
                        System.out.println("내부 IP: " + inet.getHostAddress());
                    }
                }
            }

            // ✅ 추가: 세션 매니저 생성 (최대 사용자 3명 제한)
            SessionManager sessionManager = new SessionManager(3);

            // ✅ 기존: 서버 소켓 바인딩
            ServerSocket serverSocket = new ServerSocket(PORT, 50, InetAddress.getByName("0.0.0.0"));
            System.out.println("서버 시작됨. 포트 " + PORT + " 대기 중...");

            // ✅ 기존 + 수정: 클라이언트 접속 처리
            while (true) {
                Socket clientSocket = serverSocket.accept();

                new Thread(new ClientHandler(clientSocket, sessionManager)).start();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}