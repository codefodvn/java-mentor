<%@ page import="codefod.com.controller.UserController" %>
<%@ page language="java" contentType="text/html; charset=UTF-8" pageEncoding="UTF-8"%>
<!DOCTYPE html>
<html>
<head>
    <meta charset="UTF-8">
    <title>User List</title>
</head>
<body>
<h1>User List</h1>

<table>
    <thead>
    <tr>
        <th>First Name</th>
        <th>Last Name</th>
        <th>Email</th>
        <th>Action</th>
    </tr>
    </thead>
    <tbody>
    <% for (UserController.User user : users) { %>
    <tr>
        <td><%= user.getFirstName() %></td>
        <td><%= user.getLastName() %></td>
        <td><%= user.getEmail() %></td>
        <td><a href="/users/view/<%= user.getId() %>">View</a></td>
    </tr>
    <% } %>
    </tbody>
</table>

<p><a href="/users/add">Add New User</a></p>
</body>
</html>