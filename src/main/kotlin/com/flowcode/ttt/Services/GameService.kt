package com.flowcode.ttt.Services

import com.flowcode.ttt.POJOs.Game
import com.flowcode.ttt.POJOs.Move
import com.flowcode.ttt.Repositories.GameRepository
import com.flowcode.ttt.Repositories.MoveRepository
import com.sun.org.apache.xpath.internal.operations.Bool
import org.springframework.stereotype.Service
import java.lang.Exception
import java.util.*
import kotlin.collections.ArrayList

@Service
class GameService(val gameRepository: GameRepository, val playerService: PlayerService, val moveRepository: MoveRepository) {

    fun create(playerId: String) {
        val game = Game(
                firstPlayer = playerService.getPlayer(playerId),
                secondPlayer = null,
                status = "pending",
                type = "normal",
                firstPlayerPieceCode = randomPieceCode()
                )
        gameRepository.save(game)
    }

    fun addPlayer(playerId: String, gameId: Long) {
        val game = gameRepository.findById(gameId)
        if (game.isPresent)
            gameRepository.save(game.get().copy(secondPlayer = playerService.getPlayer(playerId), status = "inprogress"))
    }

    private fun randomPieceCode(): Char {
        val random = Random(2)
        when (random.nextInt(2)) {
            1 -> return 'X'
            0 -> return 'O'
        }
        throw Exception("Random X or O generator failed")
    }

    fun getAll(): MutableIterable<Game> {
        return gameRepository.findAll()
    }

    fun makeMove(playerId: String, move: Move) {
        val game: Optional<Game> = gameRepository.findById(move.game.id!!)
        if (game.isPresent) {
            validatePlayer(playerId, game.get())
            validateMoveRedundancy(move)
            validateMoveInWonField(move)
        } else
            throw Exception("This game does not exist anymore. Please refresh your browser.")
    }

    private fun validatePlayer(playerId: String, game: Game) {
        if (game.firstPlayer.id == playerId || game.secondPlayer!!.id == playerId)
        else
            throw Exception("The user you are logged in with is not permitted to access this game!")
    }

    private fun validateMoveRedundancy(executedMove: Move) {
        for (pastMove: Move in moveRepository.findAllByGame(executedMove.game)) {
            if (pastMove.boardColumn == executedMove.boardColumn && pastMove.boardRow == executedMove.boardRow && pastMove.fieldColumn == executedMove.fieldColumn && pastMove.fieldRow == executedMove.fieldRow)
                throw Exception("This move has already been made.")
        }
    }

    private fun validateMoveInWonField(move: Move) {
        var bigField = MutableList(3) {MutableList(3) {MutableList(3) {MutableList(3) {false}}}}
        for (move: Move in moveRepository.findAllByPlayerAndGame(move.player, move.game)) {
            bigField[move.fieldRow][move.fieldColumn][move.fieldRow][move.fieldColumn] = true
        }
        var smallField: MutableList<MutableList<Boolean>> = bigField[move.fieldColumn][move.fieldRow]
        if (squareWin(smallField) || reverseSquaredWin(smallField) || straightWin(smallField))
            throw Exception("This field has already been won")
    }

    private fun squareWin(smallField: MutableList<MutableList<Boolean>>): Boolean {
        var index: Int = 0
        var amount: Int = 0

        while (index < 3) {
            if (smallField[index][index]) {
                amount ++
            }
            index ++
        }
        return amount > 2
    }

    private fun reverseSquaredWin(smallField: MutableList<MutableList<Boolean>>): Boolean {
        var index: Int = 0
        var reverseIndex: Int = 2
        var amount: Int = 0

        while (index < 3) {
            if (smallField[index][reverseIndex]) {
                amount += 1
            }
            index ++
            reverseIndex --
        }
        return amount > 2
    }

    private fun straightWin(smallField: MutableList<MutableList<Boolean>>): Boolean {
        val index: Int = 0

        while (index < 3) {
            if (smallField[index].filter { b -> b }.count() > 2)
                println()
        }
        return false
    }
}
