package com.unotangozero.app.domain.usecases.projects

import com.unotangozero.app.domain.models.Project
import com.unotangozero.app.domain.repositories.ProjectRepository
import javax.inject.Inject

/**
 * Use case for creating a new project.
 */
class CreateProjectUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    suspend operator fun invoke(project: Project): Long {
        require(project.name.isNotBlank()) { "Project name cannot be empty" }
        return projectRepository.createProject(project)
    }
}
