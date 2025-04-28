package service;

import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.prefs.Preferences;

public class UserSession {

    // Volatile ensures visibility across threads
    private static volatile UserSession instance;
    private static final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    private String userName;
    private String password;
    private String privileges;
    private final Preferences userPreferences;

    private UserSession(String userName, String password, String privileges) {
        this.userName = userName;
        this.password = password;
        this.privileges = privileges;
        this.userPreferences = Preferences.userRoot();
        savePreferences();
    }

    private void savePreferences() {
        lock.writeLock().lock();
        try {
            userPreferences.put("USERNAME", userName);
            userPreferences.put("PASSWORD", password);
            userPreferences.put("PRIVILEGES", privileges);
        } finally {
            lock.writeLock().unlock();
        }
    }

    public static UserSession getInstance(String userName, String password, String privileges) {
        // First check (no locking)
        if (instance == null) {
            // Lock for initialization
            lock.writeLock().lock();
            try {
                // Second check (with locking)
                if (instance == null) {
                    instance = new UserSession(userName, password, privileges);
                } else {
                    // Update existing instance if it was created by another thread
                    instance.updateCredentials(userName, password, privileges);
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        return instance;
    }

    public static UserSession getInstance(String userName, String password) {
        return getInstance(userName, password, "NONE");
    }

    private void updateCredentials(String userName, String password, String privileges) {
        lock.writeLock().lock();
        try {
            this.userName = userName;
            this.password = password;
            this.privileges = privileges;
            savePreferences();
        } finally {
            lock.writeLock().unlock();
        }
    }

    public String getUserName() {
        lock.readLock().lock();
        try {
            return this.userName;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getPassword() {
        lock.readLock().lock();
        try {
            return this.password;
        } finally {
            lock.readLock().unlock();
        }
    }

    public String getPrivileges() {
        lock.readLock().lock();
        try {
            return this.privileges;
        } finally {
            lock.readLock().unlock();
        }
    }

    public void cleanUserSession() {
        lock.writeLock().lock();
        try {
            this.userName = "";
            this.password = "";
            this.privileges = "";
            savePreferences();
        } finally {
            lock.writeLock().unlock();
        }
    }

    @Override
    public String toString() {
        lock.readLock().lock();
        try {
            return "UserSession{" +
                    "userName='" + this.userName + '\'' +
                    ", privileges=" + this.privileges +
                    '}';
        } finally {
            lock.readLock().unlock();
        }
    }
}
