package net.project.clinic.interview.application

fun String?.chekId(entity:String): Int {
        val id: String = this ?: throw IllegalArgumentException("Parameter $entity id not found")
        return id.toInt()
    }
