package com.lovely.todo.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.lovely.todo.dao.TodoDAO;
import com.lovely.todo.model.Todo;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class TodoServlet extends HttpServlet {
    private final TodoDAO todoDAO = new TodoDAO();
    private final Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        List<Todo> list = todoDAO.getAllTodos();

        Map<String, Object> result = new HashMap<>();
        result.put("success", true);
        result.put("todos", list);

        resp.getWriter().write(gson.toJson(result));
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            Todo newTodo = gson.fromJson(sb.toString(), Todo.class);
            if (newTodo == null || newTodo.getTitle() == null || newTodo.getTitle().trim().isEmpty()) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("error", "Task title cannot be empty");
                resp.getWriter().write(gson.toJson(err));
                return;
            }

            Todo saved = todoDAO.createTodo(newTodo);
            Map<String, Object> result = new HashMap<>();
            result.put("success", true);
            result.put("todo", saved);
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            resp.getWriter().write(gson.toJson(err));
        }
    }

    @Override
    protected void doPut(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        req.setCharacterEncoding("UTF-8");
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = req.getReader()) {
            String line;
            while ((line = reader.readLine()) != null) {
                sb.append(line);
            }
        }

        try {
            JsonObject json = gson.fromJson(sb.toString(), JsonObject.class);
            if (json == null || !json.has("id")) {
                resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                Map<String, Object> err = new HashMap<>();
                err.put("success", false);
                err.put("error", "Missing todo ID");
                resp.getWriter().write(gson.toJson(err));
                return;
            }

            Long id = json.get("id").getAsLong();
            boolean completed = json.has("completed") && json.get("completed").getAsBoolean();

            boolean ok = todoDAO.toggleTodo(id, completed);
            Map<String, Object> result = new HashMap<>();
            result.put("success", ok);
            result.put("id", id);
            result.put("completed", completed);
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            resp.getWriter().write(gson.toJson(err));
        }
    }

    @Override
    protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        resp.setContentType("application/json;charset=UTF-8");
        resp.setHeader("Access-Control-Allow-Origin", "*");

        String idParam = req.getParameter("id");
        if (idParam == null || idParam.trim().isEmpty()) {
            resp.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", "Missing id parameter");
            resp.getWriter().write(gson.toJson(err));
            return;
        }

        try {
            Long id = Long.parseLong(idParam);
            boolean ok = todoDAO.deleteTodo(id);
            Map<String, Object> result = new HashMap<>();
            result.put("success", ok);
            result.put("id", id);
            resp.getWriter().write(gson.toJson(result));
        } catch (Exception e) {
            resp.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            Map<String, Object> err = new HashMap<>();
            err.put("success", false);
            err.put("error", e.getMessage());
            resp.getWriter().write(gson.toJson(err));
        }
    }
}
