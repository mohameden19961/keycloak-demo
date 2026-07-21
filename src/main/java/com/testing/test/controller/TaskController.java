package com.testing.test.controller;

import com.testing.test.entity.Task;
import com.testing.test.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class TaskController {

    @Autowired
    private TaskRepository repo;

    @GetMapping("/public/health")
    public String health() {
        return "ok";
    }

    @GetMapping("/tasks")
    public List<Task> list() {
        return repo.findAll();
    }

    @PostMapping("/tasks")
    public Task create(@RequestBody Task t, @AuthenticationPrincipal Jwt jwt) {
        String ownerId = jwt.getClaimAsString("preferred_username");
        if (ownerId == null) ownerId = jwt.getSubject();
        t.setOwnerId(ownerId);
        return repo.save(t);
    }

    @DeleteMapping("/tasks/{id}")
    public void deleteOwn(@PathVariable Long id, @AuthenticationPrincipal Jwt jwt) {
        Task task = repo.findById(id).orElseThrow();
        String ownerId = jwt.getClaimAsString("preferred_username");
        if (ownerId == null) ownerId = jwt.getSubject();
        if (!task.getOwnerId().equals(ownerId)) {
            throw new AccessDeniedException("Not your task");
        }
        repo.deleteById(id);
    }

    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/admin/tasks/{id}")
    public void delete(@PathVariable Long id) {
        repo.deleteById(id);
    }
}
