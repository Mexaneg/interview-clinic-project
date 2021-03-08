package net.project.clinic.interview.application.dto

import org.jetbrains.exposed.dao.id.EntityID

data class PriceRequestDTO(val examinationId: Int, val clinicId: Int, val price: Long)
