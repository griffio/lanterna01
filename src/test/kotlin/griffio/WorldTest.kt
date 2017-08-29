package griffio

import org.junit.Assert
import org.junit.Test
import java.util.StringJoiner

class WorldTest {

    @Test
    fun randomWorldView() {
        val world = WorldBuilder(80, 40).build()
        println("world = $world")
    }
    /**
     * [21 29] [22 29] [23 29]
     * [21 30] [22 30] [23 30]
     * [21 31] [22 31] [23 31]
     */
    @Test
    fun blockCordsSequence() {
        val actual = blockCords(22, 30)
        Assert.assertEquals("(21, 29) (21, 30) (21, 31) (22, 29) (22, 30) (22, 31) (23, 29) (23, 30) (23, 31)", actual)
    }

    private fun blockCords(x: Int, y: Int): String {
        return join {
            (x - 1..x + 1).forEach { xn ->
                (y - 1..y + 1).forEach { yn ->
                    add(Pair(xn, yn).toString())
                }
            }
        }
    }

    private fun join(build: StringJoiner.() -> Unit): String {
        val joiner = StringJoiner(" ")
        joiner.build()
        return joiner.toString()
    }

    @Test
    fun x() {
        val worldXY = WorldXY(10, 6)
        (0 until worldXY.sizeX).forEachIndexed { idx, _ ->
            worldXY.setTileType(idx, 0, '┃')
            worldXY.setTileType(idx, worldXY.sizeY-1, '┃')
        }

        (0 until worldXY.sizeY).forEachIndexed { idx, _ ->
            worldXY.setTileType(0, idx, '━')
            worldXY.setTileType(worldXY.sizeX-1, idx, '━')
        }
        worldXY.setTileType(0, 0, '┏')
        worldXY.setTileType(0, worldXY.sizeY-1, '┓')
        worldXY.setTileType(worldXY.sizeX-1, 0, '┗')
        worldXY.setTileType(worldXY.sizeX-1, worldXY.sizeY-1, '┛')
        println(worldXY)
    }
}
