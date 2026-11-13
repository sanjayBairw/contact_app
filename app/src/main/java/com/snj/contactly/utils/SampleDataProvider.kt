package com.snj.contactly.utils

import com.snj.contactly.data.local.ContactEntity

object SampleDataProvider {

    fun createSampleContacts(now: Long = System.currentTimeMillis()): List<ContactEntity> {
        return listOf(
            ContactEntity(
                name = "Aarav Sharma",
                phone = "+91 98765 43210",
                email = "aarav.sharma@pynexia.dev",
                company = "Pynexia Labs",
                jobTitle = "Android Tech Lead",
                notes = "Mentoring Week 2 RecyclerView and Room persistence module.",
                isFavorite = true,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Clara Bennett",
                phone = "+1 (415) 890-2341",
                email = "clara.bennett@northstar.io",
                company = "Northstar Design",
                jobTitle = "Product Designer",
                notes = "Shared Material 3 typography and color tokens.",
                isFavorite = true,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Daniel Kim",
                phone = "+1 (206) 555-0194",
                email = "daniel.kim@cloudmesh.co",
                company = "CloudMesh",
                jobTitle = "Backend Engineer",
                notes = "API sync planning next Tuesday.",
                isFavorite = false,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Elena Rostova",
                phone = "+44 7700 900412",
                email = "elena.rostova@atelier.co.uk",
                company = "Atelier Studio",
                jobTitle = "Creative Director",
                notes = "Prefers email for design review feedback.",
                isFavorite = true,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Marcus Vance",
                phone = "+1 (312) 555-0148",
                email = "marcus.vance@finpeak.com",
                company = "FinPeak Analytics",
                jobTitle = "Data Architect",
                notes = "Met at Mobile Dev Summit.",
                isFavorite = false,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Nadia Patel",
                phone = "+91 98201 11223",
                email = "nadia.patel@brightware.in",
                company = "Brightware Systems",
                jobTitle = "QA Manager",
                notes = "Testing release checklist and regression suite.",
                isFavorite = false,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Oliver Grant",
                phone = "+1 (650) 555-0177",
                email = "oliver.grant@veridian.org",
                company = "Veridian Tech",
                jobTitle = "Engineering Manager",
                notes = "Weekly 1:1 on Thursdays at 10 AM.",
                isFavorite = false,
                isSample = true,
                createdAt = now,
                updatedAt = now
            ),
            ContactEntity(
                name = "Sofia Morales",
                phone = "+34 612 345 678",
                email = "sofia.morales@solaris.es",
                company = "Solaris Mobile",
                jobTitle = "Kotlin Developer",
                notes = "Collaborating on Jetpack Navigation architecture.",
                isFavorite = true,
                isSample = true,
                createdAt = now,
                updatedAt = now
            )
        )
    }
}

