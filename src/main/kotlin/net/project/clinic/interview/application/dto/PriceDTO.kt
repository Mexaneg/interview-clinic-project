package net.project.clinic.interview.application.dto

import net.project.clinic.interview.application.dao.Clinic
import net.project.clinic.interview.application.dao.Examination


data class PriceDTO(val examination: Examination, val clinic: Clinic, val price: Long)
