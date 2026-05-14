package com.unotangozero.app.domain.usecases.tasks

import com.unotangozero.app.domain.repositories.TaskRepository
import javax.inject.Inject

/**
 * Use case for deleting a task.
 */
class DeleteTaskUseCase @Inject constructor(
    private val taskRepository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        require(taskId > 0) { "Task ID must be valid" }
        taskRepository.deleteTask(taskId)
    }
}
