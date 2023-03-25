import java.io.File
import java.time.LocalTime
import java.time.temporal.ChronoUnit


fun main() {

    val logFolderName = inputFromPrompt("Logs folder:")
    val readLatest = (inputFromPrompt("Read what log? \nInput \"latest\" or \"debug\"").lowercase() == "latest")
    val minNotableTime = inputFromPrompt("Minimum notable time in ms?").toInt()
    val logFile = if (readLatest) File("$logFolderName/latest.log") else File("$logFolderName/debug.log")

    val lines = logFile.readLines()

    //laggyLines is map of <index, lag in ms>
    val laggyLines = getLinesWithLag(lines, minNotableTime)

    println("""
        Do you want to:
        1) Print all laggy lines?
        2) Print only the laggiest lines?
    """.trimIndent())

    when (readln()) {
        "1" -> printLaggyLines(lines, laggyLines)
        else -> printLaggiestLines(lines, laggyLines)
    }



}

fun printLaggiestLines(lines: List<String>, laggyLines: List<Pair<Int, Long>>) {

    val sortedList = getLaggiestLineIndices(laggyLines)

    val howMany = inputFromPrompt("How many do you wish to print?").toInt()

    println("Printing $howMany laggiest lines")

    for (i in 0 until howMany) {

        if (i >= sortedList.size) break

        val (index, interval) = sortedList[i]
        println("""
                The time between line $index and line ${index+1} was $interval ms
                Line $index: ${lines[index]}
                Line ${index+1}: ${lines[index+1]}

            """.trimIndent())
    }
}

fun printLaggyLines(lines: List<String>, laggyLines: List<Pair<Int, Long>>) {

    println("Printing all laggy lines, in order of lag")

    val sortedLaggyLines = getLaggiestLineIndices(laggyLines)
    for ((index, interval) in sortedLaggyLines) {
            println("""
                The time between line $index and line ${index+1} was $interval ms
                Line $index: ${lines[index]}
                Line ${index+1}: ${lines[index+1]}

            """.trimIndent())
    }
}

fun getLaggiestLineIndices(lines: List<Pair<Int, Long>>): List<Pair<Int, Long>> {
    return lines.sortedBy { (_,value) -> value}.reversed()
}

fun getLinesWithLag(lines: List<String>, minNotableTime: Int): List<Pair<Int, Long>> {

    val laggyLines = mutableListOf<Pair<Int,Long>>()

    for (i in lines.indices) {
        //Break if there isn't a next line
        if (lines.size == i+1) break

        val line = lines[i]
        val nextLine = lines[i+1]

        if (line.isBlank() || nextLine.isBlank()) continue
        if (line.first() != '[' || nextLine.first() != '[') continue

        // Example line:
        // [24Mar2023 16:55:18.570] [main/INFO] [mixin/]: Compatibility level set to JAVA_17

        val firstLineTime = getLineTime(line)
        val nextLineTime = getLineTime(nextLine)

        val interval = firstLineTime.until(nextLineTime, ChronoUnit.MILLIS)

        if (interval >= minNotableTime) laggyLines.add(Pair(i, interval))
    }

    return laggyLines

}

fun getLineTime(line: String): LocalTime {

    // 24Mar2023 16:55:18.570
    val dateTime = line.split(']').first().drop(0)
    val timeString = dateTime.split(' ').last()
    return LocalTime.parse(timeString)
}

fun inputFromPrompt(prompt: String): String {
    println(prompt)
    return readln()
}