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
import java.net.Socket;

public class ClientHandler extends Thread {
    private Socket socket;
    private BufferedReader in;
    private BufferedWriter out;
    private SessionManager sessionManager;
    private String userId = null;
    private boolean isWaiting = false;

    public ClientHandler(Socket socket, SessionManager sessionManager) {
        this.socket = socket;
        this.sessionManager = sessionManager;

        try {
            this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                if (isWaiting) {
                    Thread.sleep(500);
                    continue;
                }

                String msg = in.readLine();
                if (msg == null) break;

                if (msg.startsWith("LOGIN:")) {
                    String[] parts = msg.substring(6).split(",");
                    if (parts.length < 3) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
                    }

                    userId = parts[0].trim();
                    String password = parts[1].trim();
                    String role = parts[2].trim();

                    boolean valid = validateLogin(userId, password, role);
                    if (!valid) {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                        continue;
                    }

                    // ✅ 관리자라면 제한 없이 즉시 로그인 허용
                    if (role.equals("admin")) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("👑 관리자 " + userId + " 접속 허용됨");
                        continue;
                    }

                    // ✅ 사용자 로그인 처리
                    SessionManager.PendingClient pending = new SessionManager.PendingClient(socket, userId, out);
                    SessionManager.LoginDecision result = sessionManager.tryLogin(userId, pending);

                    if (result == SessionManager.LoginDecision.OK) {
                        out.write("LOGIN_SUCCESS");
                        out.newLine();
                        out.flush();
                        System.out.println("✅ 사용자 " + userId + " 로그인됨");
                    } else if (result == SessionManager.LoginDecision.WAIT) {
                        out.write("WAIT");
                        out.newLine();
                        out.flush();
                        System.out.println("⌛ 사용자 " + userId + " 대기 중");
                        isWaiting = true;
                    } else {
                        out.write("FAIL");
                        out.newLine();
                        out.flush();
                    }
                } else if (msg.startsWith("LOGOUT:")) {
                    String logoutUser = msg.substring(7).trim();
                    System.out.println("📤 로그아웃: " + logoutUser);
                    sessionManager.logout(logoutUser);
                    break;
                }
            }
        } catch (IOException | InterruptedException e) {
            System.out.println("❌ 연결 오류: " + e.getMessage());
            if (userId != null) sessionManager.logout(userId);
        }
    }

    private boolean validateLogin(String userId, String password, String role) {
        String fileName = role.equals("admin") ? "src/main/resources/ADMIN_LOGIN.txt" : "src/main/resources/USER_LOGIN.txt";
        try (BufferedReader reader = new BufferedReader(new FileReader(fileName))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] tokens = line.split(",");
                if (tokens.length >= 2 &&
                        tokens[0].trim().equals(userId) &&
                        tokens[1].trim().equals(password)) {
                    return true;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ 로그인 파일 오류: " + e.getMessage());
        }
        return false;
    }
}