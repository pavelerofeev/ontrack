package net.nemerosa.ontrack.graphql

import net.nemerosa.ontrack.json.isNullOrNullNode
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Integration tests around the `promotionLevel` root query.
 */
class PromotionLevelGraphQLIT : AbstractQLKTITSupport() {

    @Test
    fun `Annotated description`() {
        project {
            branch {
                val pl = promotionLevel(description = "A description linking to https://documentation.org/reference")
                val data = run("""{
                    promotionLevel(id: ${pl.id}) {
                        name
                        description
                        annotatedDescription
                    }
                }""")
                val promotion = data["promotionLevel"]
                assertEquals(pl.name, promotion["name"].textValue())
                assertEquals("A description linking to https://documentation.org/reference", promotion["description"].textValue())
                assertEquals("""A description linking to <a href="https://documentation.org/reference" target="_blank">https://documentation.org/reference</a>""", promotion["annotatedDescription"].textValue())
            }
        }
    }

    @Test
    fun `Paginated list of promotion runs`() {
        project {
            branch {
                val pl = promotionLevel()
                val build = build {
                    (1..25).forEach { no ->
                        promote(pl, description = "Run n°$no")
                    }
                }
                // Getting a paginated list of promotion runs
                val query = """
                    query PromotionRuns(${'$'}id: Int!, ${'$'}offset: Int!, ${'$'}size: Int!) {
                        promotionLevel(id: ${'$'}id) {
                            promotionRunsPaginated(offset: ${'$'}offset, size: ${'$'}size) {
                                pageInfo {
                                    totalSize
                                    previousPage { offset size }
                                    nextPage { offset size }
                                }
                                pageItems {
                                    description
                                    build {
                                        name
                                    }
                                }
                            }
                        }
                    }
                """
                // Getting the first page
                run(query, mapOf("id" to pl.id(), "offset" to 0, "size" to 10)).apply {
                    val field = path("promotionLevel").path("promotionRunsPaginated")
                    assertEquals(25, field["pageInfo"]["totalSize"].asInt())
                    assertTrue(field["pageInfo"]["previousPage"].isNullOrNullNode())
                    assertNotNull(field["pageInfo"]["nextPage"]) {
                        assertEquals(10, it["offset"].asInt())
                        assertEquals(10, it["size"].asInt())
                    }
                    val items = field["pageItems"]
                    assertEquals(10, items.size())
                    items.forEachIndexed { index, item ->
                        assertEquals(build.name, item["build"]["name"].asText())
                        assertEquals("Run n°${25 - index}", item["description"].asText())
                    }
                }
            }
        }
    }

}