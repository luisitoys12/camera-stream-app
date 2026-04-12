package tech.estacionkus.camerastream.domain.model

data class SportState(
    val teamA: TeamInfo = TeamInfo("Team A"),
    val teamB: TeamInfo = TeamInfo("Team B"),
    val period: String = "1st Half",
    val gameClockMs: Long = 0L,
    val isClockRunning: Boolean = false,
    val sportType: SportType = SportType.SOCCER
)

data class TeamInfo(
    val name: String,
    val score: Int = 0,
    val color: Long = 0xFF1E88E5
)

enum class SportType(val displayName: String, val periods: List<String>) {
    SOCCER("Soccer", listOf("1st Half", "2nd Half", "Extra Time")),
    BASKETBALL("Basketball", listOf("Q1", "Q2", "Q3", "Q4", "OT")),
    BASEBALL("Baseball", listOf("1st", "2nd", "3rd", "4th", "5th", "6th", "7th", "8th", "9th")),
    BOXING("Boxing", listOf("R1", "R2", "R3", "R4", "R5", "R6", "R7", "R8", "R9", "R10", "R11", "R12")),
    GENERIC("Generic", listOf("Period 1", "Period 2", "Period 3"))
}
