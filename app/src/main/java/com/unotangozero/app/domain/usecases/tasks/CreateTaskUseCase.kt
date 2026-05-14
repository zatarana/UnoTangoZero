package com.unotangozero.app.domain.usecases.tasks

import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.TaskRepository
import javax.inject.Inject

/**
 * Use case for creating a new task.
 */
class CreateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Long {
        require(task.title.isNotBlank()) { "Task title cannot be empty" }
        return taskRepository.createTask(task)
    }
}
