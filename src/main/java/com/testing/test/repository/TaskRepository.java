package com.testing.test.repository;

import com.testing.test.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TaskRepository extends JpaRepository<Task, Long> {
    List<Task> findByOwnerId(String ownerId);
}
