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
    class Wall() : Tile("\u2593", Terminal.Color.CYAN)
    class Bounds() : Tile("x", Terminal.Color.BLACK)
}

class WorldBuilder(val height: Int, val width: Int) {

    var worldTiles: Table<Int, Int, Tile> = randomWorld(ArrayTable.create<Int, Int, Tile>(0..height - 1, 0..width - 1))

    val smoothing = 1..8

    val offset = -1..1

    fun build(): WorldView {
        return WorldView(smooth(worldTiles))
    }

    fun randomWorld(tiles: Table<Int, Int, Tile>): Table<Int, Int, Tile> {
        return Tables.transformValues(tiles, { tile -> if (Math.random() < 0.5) Tile.Floor() else Tile.Wall() })
    }

    fun smooth(tiles: Table<Int, Int, Tile>): Table<Int, Int, Tile> {

        var result = tiles

        val tiles2 = ArrayTable.create<Int, Int, Tile>(0..height - 1, 0..width - 1)

        smoothing.forEach {
            for (tileCell in result.cellSet()) {
                val col = tileCell.columnKey
                val row = tileCell.rowKey
                var floors: Int = 0
                var walls: Int = 0
                offset.forEach { ox ->
                    (offset.forEach { oy ->
                        when (result.get(row?.plus(oy), col?.plus(ox))) {
                            is Tile.Floor -> floors = floors.inc()
                            is Tile.Wall -> walls = walls.inc()
                        }
                    })
                }
                tiles2.put(tileCell.rowKey, tileCell.columnKey,
                        if (floors >= walls) Tile.Floor() else Tile.Wall())
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

class WorldWriter(val screen: Screen) : ScreenWriter(screen) {

    init {
        backgroundColor = Terminal.Color.DEFAULT
    }

    fun drawWorld() {

        val world = WorldBuilder(screen.terminalSize.rows, screen.terminalSize.columns).build()

        val tiles = world.tiles.cellSet()

        for (tile in tiles) {
            screen.putString(tile.columnKey!!, tile.rowKey!!, tile.value?.char, tile.value?.color, Terminal.Color.DEFAULT)
        }
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
    val worldWriter = WorldWriter(screen)

    screen.setPaddingCharacter(' ', Terminal.Color.DEFAULT, Terminal.Color.DEFAULT)
    screen.startScreen()

    worldWriter.drawWorld()

    draw(writerDefault)
    screen.refresh()

    var key = term.readInput()

    while (key == null) {
        if (screen.resizePending()) {
            screen.clear()
            worldWriter.drawWorld()
            draw(writerDefault)
            screen.refresh()
        }

        Thread.sleep(25)
        key = term.readInput()
    }

    screen.stopScreen()
    System.exit(0)
}

fun draw(writer: DefaultWriter) {
    writer.centerString("Press any key to quit...", ScreenCharacterStyle.Blinking)
    writer.drawString(0, writer.screen.terminalSize.rows - 1, "griffio 2016", ScreenCharacterStyle.Underline)
}
