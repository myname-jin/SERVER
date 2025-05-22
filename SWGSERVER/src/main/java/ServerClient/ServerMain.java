/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package ServerClient;



// 서버를 시작하고 클라이언트 연결을 수락하는 진입점 클래스


import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 서버 진입점 클래스.
 * 포트 9999에서 외부 클라이언트의 접속을 수락하고,
 * 각 클라이언트 요청을 새로운 스레드로 처리.
 */
public class ServerMain {

    private static final int PORT = 9999;
    private static final int MAX_USERS = 3;

    public static void main(String[] args) {
        ExecutorService executorService = Executors.newCachedThreadPool();

        try {
            // 자격 인증 저장소 및 세션 관리 객체 생성
            CredentialStore credentialStore = new CredentialStore(); // USER_LOGIN.txt 읽기
            SessionManager sessionManager = new SessionManager(MAX_USERS);

            // 외부 접속을 허용하는 서버소켓 생성
            InetAddress bindAddress = InetAddress.getByName("0.0.0.0");
            ServerSocket serverSocket = new ServerSocket(PORT, 50, bindAddress);

            System.out.println("✅ 서버가 포트 " + PORT + "에서 시작되었습니다. (외부 접속 허용)");

            // 클라이언트 연결 루프
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("🔗 새로운 클라이언트 접속: " + clientSocket.getInetAddress());

                // 각 클라이언트 요청은 새로운 스레드로 처리
                executorService.execute(new ClientHandler(clientSocket, credentialStore, sessionManager));
            }

        } catch (IOException e) {
            System.err.println("❌ 서버 오류: " + e.getMessage());
            e.printStackTrace();
        }
    }
}