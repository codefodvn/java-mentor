package codefod.com.controller;

import codefod.com.core.http.HttpRequest;
import codefod.com.core.http.HttpResponse;
import codefod.com.core.mvc.ModelAndView;
import codefod.com.core.mvc.annotation.Controller;
import codefod.com.core.mvc.annotation.GetMapping;
import codefod.com.core.mvc.annotation.PostMapping;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Controller("/users")
public class UserController {

    private final Map<Integer, User> users = new HashMap<>();
    private int nextId = 1;

    public UserController() {
        // Add some sample users
        users.put(nextId++, new User("John", "Doe", "john@example.com"));
        users.put(nextId++, new User("Jane", "Smith", "jane@example.com"));
    }

    @GetMapping("/list")
    public ModelAndView listUsers() {
        ModelAndView modelAndView = new ModelAndView("users/list");
        modelAndView.addAttribute("users", users.values());
        return modelAndView;
    }

    @GetMapping("/view/{id}")
    public ModelAndView viewUser(HttpRequest request, HttpResponse response) {
        // Extract path variable manually (simplified)
        String path = request.getPath();
        String idStr = path.substring(path.lastIndexOf('/') + 1);
        int id = Integer.parseInt(idStr);

        User user = users.get(id);
        if (user == null) {
            response.setStatusCode(404);
            return new ModelAndView("404");
        }

        ModelAndView modelAndView = new ModelAndView("users/view");
        modelAndView.addAttribute("user", user);
        return modelAndView;
    }

    @GetMapping("/add")
    public String showAddForm() {
        return "users/add";
    }

    @PostMapping("/add")
    public String addUser(HttpRequest request, HttpResponse response) throws IOException {
        String firstName = request.getParameter("firstName");
        String lastName = request.getParameter("lastName");
        String email = request.getParameter("email");

        if (firstName == null || lastName == null || email == null) {
            return "users/add";
        }

        User newUser = new User(firstName, lastName, email);
        users.put(nextId++, newUser);

        // Redirect to list page
        response.setStatusCode(302);
        response.setHeader("Location", "/users/list");
        return null;
    }

    // Simple User class
    public static class User {
        private String firstName;
        private String lastName;
        private String email;

        public User(String firstName, String lastName, String email) {
            this.firstName = firstName;
            this.lastName = lastName;
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return firstName + " " + lastName + " (" + email + ")";
        }
    }
}


