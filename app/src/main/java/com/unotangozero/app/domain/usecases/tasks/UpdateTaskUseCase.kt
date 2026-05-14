package com.unotangozero.app.domain.usecases.tasks

import com.unotangozero.app.domain.models.Task
import com.unotangozero.app.domain.repositories.TaskRepository
import javax.inject.Inject

/**
 * Use case for updating an existing task.
 */
class UpdateTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(task: Task) {
        require(task.id > 0) { "Task must have a valid ID" }
        require(task.title.isNotBlank()) { "Task title cannot be empty" }
        taskRepository.updateTask(task)
    }
}
