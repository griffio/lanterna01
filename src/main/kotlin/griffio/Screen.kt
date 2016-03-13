package griffio

import com.google.common.collect.ArrayTable
import com.google.common.collect.Table
import com.google.common.collect.Tables
import com.googlecode.lanterna.TerminalFacade
import com.googlecode.lanterna.screen.Screen
import com.googlecode.lanterna.screen.ScreenCharacterStyle
import com.googlecode.lanterna.screen.ScreenWriter
import com.googlecode.lanterna.terminal.Terminal

sealed class Tile(val char: String, val color: Terminal.Color) {
    class Floor() : Tile("\u00B7", Terminal.Color.YELLOW)
    class Wall() : Tile("\u2588", Terminal.Color.CYAN)
    class Bounds() : Tile("x", Terminal.Color.BLACK)
}

class WorldBuilder(val height: Int, val width: Int) {

    var worldTiles: Table<Int, Int, Tile> = randomWorld(ArrayTable.create<Int, Int, Tile>(0..height - 1, 0..width - 1))

    fun build(): WorldView {
        return WorldView(smooth(worldTiles))
    }

    fun randomWorld(tiles: Table<Int, Int, Tile>): Table<Int, Int, Tile> {
        return Tables.transformValues(tiles, { tile -> if (Math.random() < 0.5) Tile.Floor() else Tile.Wall() })
    }

    fun incrementTile(row: Int, column: Int): Int = when (worldTiles.get(row, column)) {
        is Tile.Floor -> 1
        is Tile.Wall -> -1
        else -> 0
    }

    fun smooth(tiles: Table<Int, Int, Tile>): Table<Int, Int, Tile> {

        var result = tiles

        val tiles2 = ArrayTable.create<Int, Int, Tile>(0..height - 1, 0..width - 1)

        for (time in 1..8) {
            (0..width - 1).forEach { x ->
                (0..height - 1).forEach { y ->
                    var floors: Int = 0
                    var rocks: Int = 0
                    (-1..1).forEach { ox ->
                        (-1..1).forEach { oy ->
                            when (result.get(x + ox, y + oy)) {
                                is Tile.Floor -> floors++
                                is Tile.Wall -> rocks++
                            }
                        }
                    }
                    tiles2.put(y, x, if (floors >= rocks) Tile.Floor() else Tile.Wall())
                }
            }
            result = tiles2
        }
        return result
    }
}

class WorldView(val tiles: Table<Int, Int, Tile>) {
}

class DefaultWriter(val screen: Screen) : ScreenWriter(screen) {
    init {
        backgroundColor = Terminal.Color.DEFAULT
    }

    fun centerString(text: String, vararg styles: ScreenCharacterStyle) {
        val x = (screen.terminalSize.columns - text.length) / 2
        val y = screen.terminalSize.rows / 2
        super.drawString(x, y, text, *styles)
    }
}

fun main(args: Array<String>) {
    val term = TerminalFacade.createTerminal()
    val screen = Screen(term)
    val writerDefault = DefaultWriter(screen)

    screen.setPaddingCharacter(' ', Terminal.Color.DEFAULT, Terminal.Color.DEFAULT)
    screen.startScreen()

    val world = WorldBuilder(screen.terminalSize.rows, screen.terminalSize.columns).build()

    val tiles = world.tiles.cellSet()

    for (tile in tiles) {
        screen.putString(tile.columnKey!!, tile.rowKey!!, tile.value?.char, tile.value?.color, Terminal.Color.DEFAULT)
    }

    draw(writerDefault)
    screen.refresh()

    var key = term.readInput()

    while (key == null) {
        if (screen.resizePending()) {
            screen.clear()
            draw(writerDefault)
            screen.refresh()
        }

        Thread.sleep(5)
        key = term.readInput()
    }

    screen.stopScreen()
    System.exit(0)
}

fun draw(writer: DefaultWriter) {
    writer.centerString("Press any key to quit...", ScreenCharacterStyle.Blinking)
    writer.drawString(0, writer.screen.terminalSize.rows -1, "griffio 2016", ScreenCharacterStyle.Underline)
}
