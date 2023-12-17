package com.example

import com.example.models.ApiResponse
import com.example.plugins.*
import com.example.repository.HeroRepository
import com.example.repository.HeroRepositoryImpl
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import io.ktor.server.testing.*
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.decodeFromString
import kotlinx.serialization.json.Json
import org.apache.http.protocol.HTTP
import org.koin.java.KoinJavaComponent.inject
import kotlin.test.*

class ApplicationTest {

    @Test
    fun `access root endpoint, assert correct information`() = testApplication {

        val response = client.get("/")
            assertEquals(HttpStatusCode.OK, response.status)
            assertEquals("Welcome to Boruto API!", response.bodyAsText())

    }


    @ExperimentalSerializationApi
    @Test
    fun `access all heroes, assert correct information`() = testApplication {
        val heroRepository = HeroRepositoryImpl()


        val response = client.get("/boruto/heroes")
        assertEquals(
            expected = HttpStatusCode.OK,
            actual = response.status
        )

        val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())

        val expected = ApiResponse(
            success = true,
            message = "ok",
            prevPage = null,
            nextPage = 2,
            heroes = heroRepository.page1,
            lastUpdated = actual.lastUpdated
        )

        assertEquals(
            expected = expected,
            actual = actual
        )

    }

    @Test
    fun `access all heroes endpoint, query all pages, assert correct information`() = testApplication {
        val heroRepository = HeroRepositoryImpl()
        val pages = 1..5
        val heroes = listOf(
            heroRepository.page1,
            heroRepository.page2,
            heroRepository.page3,
            heroRepository.page4,
            heroRepository.page5,
        )

        pages.forEach { page ->
            val response = client.get("/boruto/heroes?page=$page")
            assertEquals(
                expected = HttpStatusCode.OK,
                actual = response.status
            )

            val actual = Json.decodeFromString<ApiResponse>(response.bodyAsText())

            val expected = ApiResponse(
                success = true,
                message = "ok",
                prevPage = heroRepository.calculatePrevPage(page = page),
                nextPage = heroRepository.calculateNextPage(page = page),
                heroes = heroes[page - 1],
                lastUpdated = actual.lastUpdated
            )

            assertEquals(
                expected = expected,
                actual = actual
            )

        }

    }


    @Test
    fun `acces all heroes endpoint, query non existing page number, assert error`() = testApplication {
        val response = client.get("/boruto/heroes?page=6")
        assertEquals(
            expected = HttpStatusCode.NotFound,
            actual = response.status
        )

        assertEquals(
            expected = "Page Not Found.",
            actual = response.bodyAsText()
        )
    }

}
















