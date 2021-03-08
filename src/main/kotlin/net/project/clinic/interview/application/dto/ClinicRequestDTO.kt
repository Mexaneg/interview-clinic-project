package net.project.clinic.interview.application.dto

data class ClinicRequestDTO(
    val name: String,
    val email: String,
    val phoneNumber: String,
    val city: String,
    val address: String
)