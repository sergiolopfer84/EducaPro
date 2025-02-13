/*
 * package es.prw.services;
 * 
 * import org.springframework.stereotype.Service; import java.util.HashMap;
 * import java.util.Map;
 * 
 * @Service public class LoginAttemptService { private final int MAX_ATTEMPTS =
 * 3; private final long LOCK_TIME_DURATION = 30 * 1000; // 30 segundos en
 * milisegundos private Map<String, Integer> attemptsCache = new HashMap<>();
 * private Map<String, Long> lockTimeCache = new HashMap<>();
 * 
 * public void loginFailed(String username) { int attempts =
 * attemptsCache.getOrDefault(username, 0); attempts++;
 * attemptsCache.put(username, attempts);
 * 
 * if (attempts >= MAX_ATTEMPTS) { lockTimeCache.put(username,
 * System.currentTimeMillis()); } }
 * 
 * public void loginSucceeded(String username) { attemptsCache.remove(username);
 * lockTimeCache.remove(username); }
 * 
 * public boolean isBlocked(String username) { if
 * (!lockTimeCache.containsKey(username)) { return false; } long lockTime =
 * lockTimeCache.get(username); if (System.currentTimeMillis() - lockTime >
 * LOCK_TIME_DURATION) { lockTimeCache.remove(username);
 * attemptsCache.remove(username); return false; } return true; }
 * 
 * public long getRemainingLockTime(String username) { if
 * (!lockTimeCache.containsKey(username)) { return 0; } long lockTime =
 * lockTimeCache.get(username); return (LOCK_TIME_DURATION -
 * (System.currentTimeMillis() - lockTime)) / 1000; } }
 */



package es.prw.services;

import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class LoginAttemptService {

    private static final int MAX_ATTEMPTS = 3;
    private static final long LOCK_TIME_DURATION = 30 * 1000; // 30 segundos en milisegundos

    private final Map<String, Integer> attemptsCache = new ConcurrentHashMap<>();
    private final Map<String, Long> lockTimeCache = new ConcurrentHashMap<>();

    public synchronized void loginFailed(String username) {
        int attempts = attemptsCache.getOrDefault(username, 0) + 1;
        attemptsCache.put(username, attempts);

        if (attempts >= MAX_ATTEMPTS) {
            lockTimeCache.put(username, System.currentTimeMillis());
        }
    }

    public synchronized void loginSucceeded(String username) {
        attemptsCache.remove(username);
        lockTimeCache.remove(username);
    }

    public boolean isBlocked(String username) {
        return Optional.ofNullable(lockTimeCache.get(username))
                .map(lockTime -> {
                    if (System.currentTimeMillis() - lockTime > LOCK_TIME_DURATION) {
                        loginSucceeded(username); // Restablece el estado si ha expirado el bloqueo
                        return false;
                    }
                    return true;
                })
                .orElse(false);
    }

    public long getRemainingLockTime(String username) {
        return Optional.ofNullable(lockTimeCache.get(username))
                .map(lockTime -> Math.max((LOCK_TIME_DURATION - (System.currentTimeMillis() - lockTime)) / 1000, 0))
                .orElse(0L);
    }
}
