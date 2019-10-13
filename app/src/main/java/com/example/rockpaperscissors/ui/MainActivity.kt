package com.example.rockpaperscissors.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import com.example.rockpaperscissors.R
import com.example.rockpaperscissors.database.GameRepository
import com.example.rockpaperscissors.model.Game
import com.example.rockpaperscissors.model.GameResult
import com.example.rockpaperscissors.model.Move
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class MainActivity : AppCompatActivity() {

    private val mainScope = CoroutineScope(Dispatchers.Main)
    private lateinit var gameRepository: GameRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gameRepository = GameRepository(this)
        initViews()
    }

    private fun initViews() {
        setGameStats()
        ibRock.setOnClickListener { onClickMove(Move.rock) }
        ibPaper.setOnClickListener { onClickMove(Move.paper) }
        ibScissors.setOnClickListener { onClickMove(Move.scissors) }
    }

    private fun setGameStats() {
        var win = 0
        var draw = 0
        var lose = 0

        mainScope.launch {
            val games = withContext(Dispatchers.IO) {
                gameRepository.getAllGames()
            }

            games.forEach {
                when (it.getResult()) {
                    GameResult.win -> {
                        win += 1
                    }
                    GameResult.draw -> {
                        draw += 1
                    }
                    GameResult.lose -> {
                        lose += 1
                    }
                }
            }
            tvStats.text = getString(R.string.stats, win, draw, lose)
        }
    }

    private fun onClickMove(move: Move) {
        var game = Game(Calendar.getInstance().time, randomMove(), move)
        addGame(game)
        ivComputer.setImageDrawable(getDrawable(game.computerMove.image))
        ivYou.setImageDrawable(getDrawable(game.yourMove.image))
        setGameStats()

        tvResult.text = game.getResult().result
    }

    private fun randomMove(): Move {
        return Move.values()[Random().nextInt(Move.values().size)]
    }

    private fun addGame(game: Game) {
        mainScope.launch {
            withContext(Dispatchers.IO) {
                gameRepository.insertGame(game)
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        return when (item.itemId) {
            R.id.action_history -> {
                var intent = Intent(this, HistoryActivity::class.java)
                startActivity(intent)
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
