package com.example.exe1_oran_tausi

class GameManager (
    private val numLane: Int = 3
) {

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
        clearCones()
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
    private val cols = 3

    private var spawnThisRow = true
    private var pendingCrashLane: Int? = null



    var cones = Array(rows){ IntArray(cols){0} }

    fun step(){
        moveConesDown()
        generateNewTopRow()
    }

    fun getConesGrid(): Array<IntArray> = cones


    fun moveConesDown(){
        for( r in rows-1 downTo 1){
            for(c in 0 until cols) {
                cones[r][c] = cones[r - 1][c]
            }
        }
        for(c in 0 until cols){
            cones[0][c]=0
        }
    }
    private fun generateNewTopRow() {

        for (c in 0 until cols) cones[0][c] = 0

        if (!spawnThisRow) {
            spawnThisRow = true
            return
        }

        val randomCol = (0 until cols).random()
        cones[0][randomCol] = 1

        spawnThisRow = false
    }


    fun stepConesAndCheckCrash(): Boolean {

        pendingCrashLane?.let { lane ->
            cones[rows - 1][lane] = 0
            pendingCrashLane = null
        }

        moveConesDown()

        val crashRow = rows - 1
        val crashed = (cones[crashRow][carLane] == 1)

        if (crashed) {
            loseLife()

            pendingCrashLane = carLane
        }

        generateNewTopRow()
        return crashed
    }


    fun clearCones(){
        for(r in 0 until rows){
            for(c in 0 until cols){
                cones[r][c] = 0
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




}

