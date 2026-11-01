package com.lovely.todo.model;

import java.io.Serializable;
import java.sql.Timestamp;

public class Todo implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String title;
    private String category;
    private String priority;
    private boolean completed;
    private String dueDate;
    private Timestamp createdAt;

    public Todo() {
    }

    public Todo(Long id, String title, String category, String priority, boolean completed, String dueDate, Timestamp createdAt) {
        this.id = id;
        this.title = title;
        this.category = category;
        this.priority = priority;
        this.completed = completed;
        this.dueDate = dueDate;
        this.createdAt = createdAt;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getPriority() {
        return priority;
    }

    public void setPriority(String priority) {
        this.priority = priority;
    }

    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }

    public String getDueDate() {
        return dueDate;
    }

    public void setDueDate(String dueDate) {
        this.dueDate = dueDate;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }
}
