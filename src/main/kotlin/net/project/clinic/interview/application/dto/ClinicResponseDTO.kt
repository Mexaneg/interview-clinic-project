package net.project.clinic.interview.application.dto

import org.jetbrains.exposed.dao.id.EntityID

data class ClinicResponseDTO(
    val clinicId: EntityID<Int>,
    val name: String,
    val email: String,
    val phoneNumber: String,
    val city: String,
    val address: String,
    //val pricing: Map<UUID, Long>
)
