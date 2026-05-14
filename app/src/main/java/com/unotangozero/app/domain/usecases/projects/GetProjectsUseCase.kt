package com.unotangozero.app.domain.usecases.projects

import com.unotangozero.app.domain.models.Project
import com.unotangozero.app.domain.repositories.ProjectRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Use case for fetching all projects.
 */
class GetProjectsUseCase @Inject constructor(
    private val projectRepository: ProjectRepository
) {
    operator fun invoke(): Flow<List<Project>> {
        return projectRepository.getAllProjects()
    }
}
