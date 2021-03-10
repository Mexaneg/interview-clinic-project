package net.project.clinic.interview.application.dto

import org.jetbrains.exposed.dao.id.EntityID

data class ClinicResponseDTO(
    val clinicId: Int,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val city: String,
    val address: String,
)
