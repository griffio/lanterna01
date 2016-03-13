package griffio

import org.junit.Test
import kotlin.test.assertEquals

class WorldTest {

    @Test
    fun randomWorldView() {
        val world = WorldBuilder(80, 40).build()
        println("world = ${world}")
    }
}
