package com.unotangozero.app.domain.usecases.projects

import com.unotangozero.app.domain.repositories.ProjectRepository
import javax.inject.Inject

/**
 * Use case for updating project progress.
 */
class UpdateProjectProgressUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(projectId: Long, progress: Int) {
        require(projectId > 0) { "Project ID must be valid" }
        require(progress in 0..100) { "Progress must be between 0 and 100" }
        projectRepository.updateProjectProgress(projectId, progress)
    }
}
