package com.lovely.todo.dao;

import com.lovely.todo.model.Todo;

import java.sql.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

public class TodoDAO {
    private static final Map<Long, Todo> memoryStore = new ConcurrentHashMap<>();
    private static final AtomicLong idGenerator = new AtomicLong(100);

    static {
        Todo t1 = new Todo(1L, "Design pink & white aesthetic UI 🌸", "Personal 🌷", "HIGH", true, "Today", new Timestamp(System.currentTimeMillis() - 3600000));
        Todo t2 = new Todo(2L, "Connect EC2 MySQL database 💖", "Work 💖", "HIGH", false, "Today", new Timestamp(System.currentTimeMillis()));
        Todo t3 = new Todo(3L, "Deploy WAR package on Tomcat server 🎀", "Work 💖", "MEDIUM", false, "Tomorrow", new Timestamp(System.currentTimeMillis() + 1000));
        Todo t4 = new Todo(4L, "Celebrate with strawberry cupcakes! 🧁✨", "Celebration ✨", "LOW", false, "Weekend", new Timestamp(System.currentTimeMillis() + 2000));
        memoryStore.put(t1.getId(), t1);
        memoryStore.put(t2.getId(), t2);
        memoryStore.put(t3.getId(), t3);
        memoryStore.put(t4.getId(), t4);
    }

    public List<Todo> getAllTodos() {
        List<Todo> list = new ArrayList<>();
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "SELECT id, title, category, priority, completed, due_date, created_at FROM todos ORDER BY id DESC";
            try (Statement stmt = conn.createStatement();
                 ResultSet rs = stmt.executeQuery(sql)) {
                while (rs.next()) {
                    Todo todo = new Todo(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("category"),
                        rs.getString("priority"),
                        rs.getBoolean("completed"),
                        rs.getString("due_date"),
                        rs.getTimestamp("created_at")
                    );
                    list.add(todo);
                }
            }
            return list;
        } catch (Exception e) {
            List<Todo> fallbackList = new ArrayList<>(memoryStore.values());
            fallbackList.sort((a, b) -> b.getId().compareTo(a.getId()));
            return fallbackList;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public Todo createTodo(Todo todo) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "INSERT INTO todos (title, category, priority, completed, due_date, created_at) VALUES (?, ?, ?, ?, ?, ?)";
            try (PreparedStatement ps = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
                ps.setString(1, todo.getTitle());
                ps.setString(2, todo.getCategory() != null ? todo.getCategory() : "Personal 🌷");
                ps.setString(3, todo.getPriority() != null ? todo.getPriority() : "MEDIUM");
                ps.setBoolean(4, todo.isCompleted());
                ps.setString(5, todo.getDueDate());
                Timestamp now = new Timestamp(System.currentTimeMillis());
                ps.setTimestamp(6, now);
                ps.executeUpdate();

                try (ResultSet generatedKeys = ps.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        todo.setId(generatedKeys.getLong(1));
                    }
                }
                todo.setCreatedAt(now);
                return todo;
            }
        } catch (Exception e) {
            long newId = idGenerator.incrementAndGet();
            todo.setId(newId);
            todo.setCreatedAt(new Timestamp(System.currentTimeMillis()));
            memoryStore.put(newId, todo);
            return todo;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean toggleTodo(Long id, boolean completed) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "UPDATE todos SET completed = ? WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setBoolean(1, completed);
                ps.setLong(2, id);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            Todo t = memoryStore.get(id);
            if (t != null) {
                t.setCompleted(completed);
                return true;
            }
            return false;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }

    public boolean deleteTodo(Long id) {
        Connection conn = null;
        try {
            conn = DBUtil.getConnection();
            String sql = "DELETE FROM todos WHERE id = ?";
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setLong(1, id);
                return ps.executeUpdate() > 0;
            }
        } catch (Exception e) {
            return memoryStore.remove(id) != null;
        } finally {
            if (conn != null) {
                try { conn.close(); } catch (SQLException ignored) {}
            }
        }
    }
}
