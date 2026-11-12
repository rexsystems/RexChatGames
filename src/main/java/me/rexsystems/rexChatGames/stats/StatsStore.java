package me.rexsystems.rexChatGames.stats;

import me.rexsystems.rexChatGames.game.GameType;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public final class StatsStore {

    public record WinRow(String name, int wins) {
    }

    public record RecordRow(String name, long millis) {
    }

    private final File file;
    private final java.util.logging.Logger logger;
    private final Object lock = new Object();
    private final ConcurrentHashMap<UUID, Boolean> muted = new ConcurrentHashMap<>();
    private Connection connection;

    public StatsStore(File file, java.util.logging.Logger logger) {
        this.file = file;
        this.logger = logger;
    }

    public void open() {
        synchronized (lock) {
            try {
                Class.forName("me.rexsystems.rexChatGames.libs.sqlite.JDBC");
                if (file.getParentFile() != null) {
                    file.getParentFile().mkdirs();
                }
                connection = DriverManager.getConnection("jdbc:sqlite:" + file.getAbsolutePath());
                try (Statement statement = connection.createStatement()) {
                    statement.execute("PRAGMA busy_timeout = 5000");
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS wins (
                              uuid TEXT NOT NULL,
                              name TEXT NOT NULL,
                              type TEXT NOT NULL,
                              wins INTEGER NOT NULL,
                              best_millis INTEGER,
                              PRIMARY KEY (uuid, type)
                            )
                            """);
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS records (
                              type TEXT PRIMARY KEY,
                              uuid TEXT NOT NULL,
                              name TEXT NOT NULL,
                              millis INTEGER NOT NULL
                            )
                            """);
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS log (
                              id INTEGER PRIMARY KEY AUTOINCREMENT,
                              at INTEGER NOT NULL,
                              type TEXT NOT NULL,
                              uuid TEXT,
                              name TEXT,
                              millis INTEGER,
                              outcome TEXT NOT NULL
                            )
                            """);
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS last_run (
                              type TEXT PRIMARY KEY,
                              at INTEGER NOT NULL
                            )
                            """);
                    statement.execute("""
                            CREATE TABLE IF NOT EXISTS muted (
                              uuid TEXT PRIMARY KEY
                            )
                            """);
                }
                loadMutes();
            } catch (Throwable failure) {
                connection = null;
                logger.warning("Stats database is unavailable: " + failure.getMessage());
            }
        }
    }

    public void close() {
        synchronized (lock) {
            if (connection == null) {
                return;
            }
            try {
                connection.close();
            } catch (SQLException ignored) {
            }
            connection = null;
        }
    }

    public boolean isMuted(UUID uuid) {
        return uuid != null && muted.containsKey(uuid);
    }

    public boolean toggle(UUID uuid) {
        if (uuid == null) {
            return false;
        }
        boolean hidden = muted.remove(uuid) == null;
        if (hidden) {
            muted.put(uuid, Boolean.TRUE);
        }
        synchronized (lock) {
            if (connection == null) {
                return hidden;
            }
            try {
                if (hidden) {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "INSERT OR IGNORE INTO muted (uuid) VALUES (?)")) {
                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                } else {
                    try (PreparedStatement statement = connection.prepareStatement(
                            "DELETE FROM muted WHERE uuid = ?")) {
                        statement.setString(1, uuid.toString());
                        statement.executeUpdate();
                    }
                }
            } catch (SQLException failure) {
                logger.warning("Could not save toggle: " + failure.getMessage());
            }
        }
        return hidden;
    }

    public void markStarted(GameType type) {
        if (type == null) {
            return;
        }
        synchronized (lock) {
            if (connection == null) {
                return;
            }
            try (PreparedStatement statement = connection.prepareStatement("""
                    INSERT INTO last_run (type, at) VALUES (?, ?)
                    ON CONFLICT(type) DO UPDATE SET at = excluded.at
                    """)) {
                statement.setString(1, type.id());
                statement.setLong(2, System.currentTimeMillis());
                statement.executeUpdate();
            } catch (SQLException failure) {
                logger.warning("Could not store last run: " + failure.getMessage());
            }
        }
    }

    public void recordWin(UUID uuid, String name, GameType type, long millis) {
        if (uuid == null || type == null) {
            return;
        }
        String safeName = name == null || name.isBlank() ? uuid.toString() : name;
        synchronized (lock) {
            if (connection == null) {
                return;
            }
            try {
                try (PreparedStatement statement = connection.prepareStatement(
                        "UPDATE wins SET name = ? WHERE uuid = ?")) {
                    statement.setString(1, safeName);
                    statement.setString(2, uuid.toString());
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO wins (uuid, name, type, wins, best_millis) VALUES (?, ?, ?, 1, ?)
                        ON CONFLICT(uuid, type) DO UPDATE SET
                          wins = wins + 1,
                          name = excluded.name,
                          best_millis = CASE
                            WHEN wins.best_millis IS NULL OR excluded.best_millis < wins.best_millis THEN excluded.best_millis
                            ELSE wins.best_millis
                          END
                        """)) {
                    statement.setString(1, uuid.toString());
                    statement.setString(2, safeName);
                    statement.setString(3, type.id());
                    statement.setLong(4, Math.max(0L, millis));
                    statement.executeUpdate();
                }
                try (PreparedStatement statement = connection.prepareStatement("""
                        INSERT INTO records (type, uuid, name, millis) VALUES (?, ?, ?, ?)
                        ON CONFLICT(type) DO UPDATE SET
                          uuid = excluded.uuid,
                          name = excluded.name,
                          millis = excluded.millis
                        WHERE excluded.millis < records.millis
                        """)) {
                    statement.setString(1, type.id());
                    statement.setString(2, uuid.toString());
                    statement.setString(3, safeName);
                    statement.setLong(4, Math.max(0L, millis));
                    statement.executeUpdate();
                }
                logLocked(type, uuid, safeName, millis, "win");
            } catch (SQLException failure) {
                logger.warning("Could not store win: " + failure.getMessage());
            }
        }
    }

    public void log(GameType type, UUID uuid, String name, Long millis, String outcome) {
        if (type == null || outcome == null) {
            return;
        }
        synchronized (lock) {
            if (connection == null) {
                return;
            }
            try {
                logLocked(type, uuid, name, millis, outcome);
            } catch (SQLException failure) {
                logger.warning("Could not store log: " + failure.getMessage());
            }
        }
    }

    public List<WinRow> topTotal(int limit) {
        return top("SELECT MAX(name) AS name, SUM(wins) AS total FROM wins GROUP BY uuid ORDER BY total DESC, name COLLATE NOCASE ASC LIMIT ?", null, limit);
    }

    public List<WinRow> topType(GameType type, int limit) {
        if (type == null) {
            return List.of();
        }
        return top("SELECT name, wins AS total FROM wins WHERE type = ? ORDER BY wins DESC, name COLLATE NOCASE ASC LIMIT ?", type.id(), limit);
    }

    public RecordRow record(GameType type) {
        if (type == null) {
            return null;
        }
        synchronized (lock) {
            if (connection == null) {
                return null;
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT name, millis FROM records WHERE type = ?")) {
                statement.setString(1, type.id());
                try (ResultSet rows = statement.executeQuery()) {
                    if (!rows.next()) {
                        return null;
                    }
                    return new RecordRow(rows.getString("name"), rows.getLong("millis"));
                }
            } catch (SQLException failure) {
                logger.warning("Could not read record: " + failure.getMessage());
                return null;
            }
        }
    }

    public int wins(UUID uuid) {
        if (uuid == null) {
            return 0;
        }
        return scalarInt("SELECT COALESCE(SUM(wins), 0) FROM wins WHERE uuid = ?", uuid.toString(), null);
    }

    public int wins(UUID uuid, GameType type) {
        if (uuid == null || type == null) {
            return 0;
        }
        return scalarInt("SELECT COALESCE(SUM(wins), 0) FROM wins WHERE uuid = ? AND type = ?", uuid.toString(), type.id());
    }

    public Long fastest(UUID uuid, GameType type) {
        if (uuid == null || type == null) {
            return null;
        }
        synchronized (lock) {
            if (connection == null) {
                return null;
            }
            try (PreparedStatement statement = connection.prepareStatement(
                    "SELECT best_millis FROM wins WHERE uuid = ? AND type = ?")) {
                statement.setString(1, uuid.toString());
                statement.setString(2, type.id());
                try (ResultSet rows = statement.executeQuery()) {
                    if (!rows.next() || rows.getObject("best_millis") == null) {
                        return null;
                    }
                    return rows.getLong("best_millis");
                }
            } catch (SQLException failure) {
                return null;
            }
        }
    }

    public Long globalRecord(GameType type) {
        RecordRow row = record(type);
        return row == null ? null : row.millis();
    }

    public Map<GameType, Long> lastRuns() {
        EnumMap<GameType, Long> runs = new EnumMap<>(GameType.class);
        for (GameType type : GameType.values()) {
            runs.put(type, 0L);
        }
        synchronized (lock) {
            if (connection == null) {
                return runs;
            }
            try (Statement statement = connection.createStatement();
                 ResultSet rows = statement.executeQuery("SELECT type, at FROM last_run")) {
                while (rows.next()) {
                    GameType type = GameType.byId(rows.getString("type"));
                    if (type != null) {
                        runs.put(type, rows.getLong("at"));
                    }
                }
            } catch (SQLException failure) {
                logger.warning("Could not read status: " + failure.getMessage());
            }
        }
        return runs;
    }

    private void loadMutes() throws SQLException {
        muted.clear();
        try (Statement statement = connection.createStatement();
             ResultSet rows = statement.executeQuery("SELECT uuid FROM muted")) {
            while (rows.next()) {
                try {
                    muted.put(UUID.fromString(rows.getString("uuid")), Boolean.TRUE);
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
    }

    private void logLocked(GameType type, UUID uuid, String name, Long millis, String outcome) throws SQLException {
        try (PreparedStatement statement = connection.prepareStatement("""
                INSERT INTO log (at, type, uuid, name, millis, outcome) VALUES (?, ?, ?, ?, ?, ?)
                """)) {
            statement.setLong(1, System.currentTimeMillis());
            statement.setString(2, type.id());
            statement.setString(3, uuid == null ? null : uuid.toString());
            statement.setString(4, name);
            if (millis == null) {
                statement.setNull(5, java.sql.Types.INTEGER);
            } else {
                statement.setLong(5, millis);
            }
            statement.setString(6, outcome);
            statement.executeUpdate();
        }
        try (Statement statement = connection.createStatement()) {
            statement.execute("""
                    DELETE FROM log WHERE id NOT IN (SELECT id FROM log ORDER BY id DESC LIMIT 40)
                    """);
        }
    }

    private List<WinRow> top(String sql, String type, int limit) {
        int size = Math.max(1, Math.min(20, limit));
        synchronized (lock) {
            if (connection == null) {
                return List.of();
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                if (type == null) {
                    statement.setInt(1, size);
                } else {
                    statement.setString(1, type);
                    statement.setInt(2, size);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    List<WinRow> result = new ArrayList<>();
                    while (rows.next()) {
                        result.add(new WinRow(rows.getString("name"), rows.getInt("total")));
                    }
                    return result;
                }
            } catch (SQLException failure) {
                logger.warning("Could not read top wins: " + failure.getMessage());
                return List.of();
            }
        }
    }

    private int scalarInt(String sql, String uuid, String type) {
        synchronized (lock) {
            if (connection == null) {
                return 0;
            }
            try (PreparedStatement statement = connection.prepareStatement(sql)) {
                statement.setString(1, uuid);
                if (type != null) {
                    statement.setString(2, type);
                }
                try (ResultSet rows = statement.executeQuery()) {
                    return rows.next() ? rows.getInt(1) : 0;
                }
            } catch (SQLException failure) {
                return 0;
            }
        }
    }
}
