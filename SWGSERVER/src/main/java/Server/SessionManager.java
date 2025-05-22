package Server;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author adsd3
 */
import java.io.BufferedWriter;
import java.net.Socket;
import java.util.*;

public class SessionManager {
    private final int maxUsers;
    private final Set<String> active = new HashSet<>();  // 사용자만 저장
    private final Queue<PendingClient> queue = new ArrayDeque<>();

    public SessionManager(int maxUsers) {
        this.maxUsers = maxUsers;
    }

    public enum LoginDecision { OK, WAIT, FAIL_DUP }

    public static class PendingClient {
        public final Socket socket;
        public final String userId;
        public final BufferedWriter out;

        public PendingClient(Socket socket, String userId, BufferedWriter out) {
            this.socket = socket;
            this.userId = userId;
            this.out = out;
        }
    }

    public synchronized LoginDecision tryLogin(String userId, PendingClient pending) {
        if (active.contains(userId)) return LoginDecision.FAIL_DUP;

        if (active.size() < maxUsers) {
            active.add(userId); // 사용자만 저장
            return LoginDecision.OK;
        } else {
            queue.offer(pending);
            return LoginDecision.WAIT;
        }
    }

    public synchronized void logout(String userId) {
        active.remove(userId);  // 사용자만 제거
        nextClient();
    }

    public synchronized void nextClient() {
        if (active.size() >= maxUsers || queue.isEmpty()) return;

        PendingClient next = queue.poll();
        try {
            next.out.write("LOGIN_SUCCESS");
            next.out.newLine();
            next.out.flush();
            active.add(next.userId);
            System.out.println("🟢 대기자 자동 로그인: " + next.userId);
        } catch (Exception e) {
            System.err.println("❌ 대기자 로그인 실패: " + e.getMessage());
        }
    }
}