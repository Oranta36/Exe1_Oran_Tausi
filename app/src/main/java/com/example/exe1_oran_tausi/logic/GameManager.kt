package com.example.exe1_oran_tausi.logic

import kotlin.random.Random

class GameManager (
    private val numLane: Int = 5
) {
    private var tickMs: Long = 800L

    fun setFastMode(isFast: Boolean) {
        tickMs = if (isFast) 400L else 800L
    }

    fun getTickMs(): Long = tickMs

    fun getDeltaTimeSeconds(): Float = tickMs / 1000f


    //////////////////////////////////////////////CarLogic/////////////////////////////////////////

    var carLane: Int = 1
        private set

    fun moveCarLeft() {
        if (carLane > 0) {
            carLane--
        }
    }

    fun moveCarRight() {
        if (carLane < numLane - 1) {
            carLane++
        }
    }


    fun resetGame() {
        carLane = 1
        currentLives = maxLives
        coinsCollected = 0
        clearItems()
        distanceMeters = 0f
    }



    ///////////////////////////////////////////hartLogic///////////////////////////////////////////

    private val maxLives: Int = 3
    var currentLives: Int = maxLives

    fun isGameOver(): Boolean = currentLives <= 0

    fun loseLife (){
        currentLives --
    }


    ////////////////////////////////////////ConusMatrix///////////////////////////////////////////

    private val rows = 7
    private val cols = 5

    private var spawnThisRow = true
    private var pendingClearLane: Int? = null

    var coinsCollected: Int = 0
    private val EMPTY = 0
    private val CONE  = 1
    private val COIN  = 2

    var items = Array(rows) { IntArray(cols) { EMPTY } }

    fun getItemsGrid(): Array<IntArray> = items

    fun moveItemsDown() {
        for (r in rows - 1 downTo 1) {
            for (c in 0 until cols) {
                items[r][c] = items[r - 1][c]
            }
        }
        for (c in 0 until cols) {
            items[0][c] = EMPTY
        }
    }

    private fun generateNewTopRow() {
        for (c in 0 until cols) items[0][c] = EMPTY

        if (!spawnThisRow) {
            spawnThisRow = true
            return
        }

        val maxPerRow = minOf(3, cols)
        val count = (1..maxPerRow).random()
        val coinChance = 0.40f
        val putCoin = (Random.nextFloat() < coinChance)

        val colsShuffled = (0 until cols).shuffled()
        var idx = 0


        var remaining = count
        if (putCoin && remaining > 0) {
            items[0][colsShuffled[idx++]] = COIN
            remaining--
        }

        repeat(remaining) {
            items[0][colsShuffled[idx++]] = CONE
        }

        spawnThisRow = false
    }

    data class StepResult(val crashed: Boolean, val collectedCoin: Boolean)

    fun stepItemsAndCheck(): StepResult {

        pendingClearLane?.let { lane ->
            items[rows - 1][lane] = EMPTY
            pendingClearLane = null
        }

        moveItemsDown()

        val lastRow = rows - 1
        val cell = items[lastRow][carLane]

        val crashed = (cell == CONE)
        val collected = (cell == COIN)

        if (crashed) {
            loseLife()
            pendingClearLane = carLane
        } else if (collected) {
            coinsCollected++
            pendingClearLane = carLane
        }

        generateNewTopRow()
        return StepResult(crashed = crashed, collectedCoin = collected)
    }

    fun clearItems() {
        for (r in 0 until rows) {
            for (c in 0 until cols) {
                items[r][c] = EMPTY
            }
        }
    }


    ///////////////////////////////////////Kilometraz/////////////////////////////////////////////

    private val speedMetersPerSecond = 30f
    var distanceMeters: Float = 0f
        private set

    fun updateDistance (deltaTimeSeconds: Float){
        if(!isGameOver()){
            distanceMeters += speedMetersPerSecond * deltaTimeSeconds
        }

    }

    fun getDistanceKm(): Float = distanceMeters/1000f

    ////////////////////////////////// Tick API /////////////////////////////////////////

    data class GameState(
        val crashedThisTick: Boolean,
        val coinCollectedThisTick: Boolean,
        val coins: Int,
        val grid: Array<IntArray>,
        val lives: Int,
        val distanceKm: Float,
        val carLane: Int,
        val isGameOver: Boolean,
        val nextTickMs: Long
    )

    fun tick(): GameState {
        val deltaTimeSeconds = getDeltaTimeSeconds()
        updateDistance(deltaTimeSeconds)

        val step = stepItemsAndCheck()

        return GameState(
            crashedThisTick = step.crashed,
            coinCollectedThisTick = step.collectedCoin,
            coins = coinsCollected,
            grid = getItemsGrid(),
            lives = currentLives,
            distanceKm = getDistanceKm(),
            carLane = carLane,
            isGameOver = isGameOver(),
            nextTickMs = tickMs
        )
    }

}

