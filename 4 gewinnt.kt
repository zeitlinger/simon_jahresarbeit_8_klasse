@file:Suppress("SpellCheckingInspection", "NonAsciiCharacters")

package gew

import kotlin.streams.toList

var brett = mutableListOf(
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0,
        0, 0, 0, 0, 0, 0, 0
)

enum class Schwierigkeit(var minMax: Int, var monteCarlo: Int) {
    leicht(minMax = 1, monteCarlo = 25000),
    mittel(minMax = 1, monteCarlo = 125000),
    schwer(minMax = 1, monteCarlo = 250000)
}


fun menschZug(züge: Int) {
    println("Du bist am Zug ${
        if (züge < 2) {
            "(1 - 7)"
        }
    else ""
    } ")
    if (züge < 2) {
        println("  1 2 3 4 5 6 7  ")
        println("+---------------+")
        var zeile = 0
        while (zeile < 6) {
            var spalte = 0
            print("| ")
            while (spalte < 7) {
                print(when (brett[zeile * 7 + spalte]) {
                    0 -> "  "
                    1 -> "o "
                    else -> "x "
                })

                spalte += 1

            }

            println("|")
            zeile += 1
        }
        println("+---------------+")
    }


while (true) {
    val eingabe = readLine()?.toIntOrNull()
    if (eingabe == null || eingabe < 1 || eingabe > 7 || brett[eingabe - 1] != 0) {
        println("Falsche Eingabe")
        continue
    }
    if (zug(eingabe - 1, brett, -1)) {
        return
    }
    println("Falsche Eingabe")
}
}

data class Knoten(val kinder: List<Knoten>, var wert: Int = 0, val brett: List<Int>, val zug: Int, val dran: Int)

fun generieren(brett: List<Int>, feld: Int, dran: Int, züge: Int, tabelle: MutableMap<List<Int>, Knoten>): Knoten {
    val alterKnoten = tabelle[brett]
    if (alterKnoten != null) {
        return alterKnoten
    }

    val kinder = mutableListOf<Knoten>()
    if (züge > 0) {
        möglichkeiten(brett).forEach { spalte ->
            val brett1 = brett.toMutableList()
            zug(spalte, brett1, dran)
            if (gewonnen(brett1, 1) || gewonnen(brett1, -1)) {
                kinder.add(Knoten(listOf(), brett = brett1, zug = spalte, dran = dran))
            } else {
                kinder.add(generieren(brett1, spalte, dran * -1, züge = züge - 1, tabelle))
            }
        }
    }

    val knoten = Knoten(kinder, brett = brett, zug = feld, dran = dran)
    tabelle[brett] = knoten
    return knoten
}

fun minMax(minMax: Int, knoten: Knoten, monteCarlo: Int) {
    if (gewonnen(knoten.brett, -1)) {
        knoten.wert = -monteCarlo - 1
        return
    } else if (gewonnen(knoten.brett, 1)) {
        knoten.wert = monteCarlo + 1
        return
    } else if (knoten.kinder.isEmpty()) {
        knoten.wert = monteCarlo(brett = knoten.brett, dran = knoten.dran * -1, knoten = knoten, monteCarlo)
    }
    knoten.kinder.forEach { kind -> minMax(minMax * -1, kind, monteCarlo) }

    if (knoten.kinder.isEmpty()) {
        return
    }

    if (minMax == 1) {
        knoten.wert = knoten.kinder.maxOf { kind -> kind.wert }
    } else {
        knoten.wert = knoten.kinder.minOf { kind -> kind.wert }
    }
}

fun monteCarlo(brett: List<Int>, dran: Int, knoten: Knoten, monteCarlo: Int): Int {
    var durchläufe = monteCarlo
    var wert = 0
    val züge = 0
    val zeit = System.currentTimeMillis()
    wert = (0..durchläufe).toList().parallelStream()
            .map { durchlauf(brett = brett.toMutableList(), dran = dran, knoten = knoten, wert = wert, züge = züge) }
            .toList().sum()
    return wert
}

fun durchlauf(brett: MutableList<Int>, dran: Int, knoten: Knoten, wert: Int, züge: Int): Int {

    var möglichkeiten = möglichkeiten(brett)
    if (möglichkeiten.isEmpty()) {
        return 0
    }

    var spalte = möglichkeiten.random()
    zug(spalte, brett, dran)
    if (gewonnen(brett, wer = -1)) {
        return -1
    }
    if (gewonnen(brett, wer = 1)) {
        return 1
    }


    return durchlauf(brett = brett, dran = dran * -1, knoten = knoten, wert = wert, züge = züge + 1)
}

private fun zug(spalte: Int, brett: MutableList<Int>, dran: Int): Boolean {
    var zeile = 5
    var index = spalte + 35
    while (zeile >= 0) {
        if (brett[index] == 0) {
            if (dran == 1) {
                brett[index] = 1
            } else {
                brett[index] = -1

            }
            return true
        } else {
            index -= 7
            zeile -= 1
        }
    }
    return false
}

private fun möglichkeiten(brett: List<Int>): MutableList<Int> {
    var möglichkeiten = mutableListOf<Int>()
    var spalte: Int = 0
    while (spalte < 7) {
        if (brett[spalte] == 0) {
            möglichkeiten.add(spalte)
        }
        spalte += 1
    }
    return möglichkeiten
}

fun computerZug(s: Schwierigkeit, zug: Int) {
    if (s == Schwierigkeit.schwer) {
        println("Bitte warten...")
    }
    if (zug < 3) {
        zug(3, brett, 1)
        return
    }
    val zeit = System.currentTimeMillis()
    var k = generieren(brett.toMutableList(), -1, 1, züge = s.minMax, mutableMapOf())
    minMax(1, k, s.monteCarlo)
    val max = k.kinder.maxOf { kind -> kind.wert }
    val liste = mutableListOf<Knoten>()
    k.kinder.forEach { kind ->
        if (kind.wert == max) {
            liste.add(kind)
        }
    }
    val zug = liste.random().zug
    zug(zug, brett, 1)

    println("zeit: " + (System.currentTimeMillis() - zeit) / 1000)
}

fun gewonnen(brett: List<Int>, wer: Int): Boolean {
    var zeile = 0
    while (zeile < 6) {
        var spalte = 0
        while (spalte < 4) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 1] == wer && brett[index + 2] == wer && brett[index + 3] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    zeile = 0
    while (zeile < 3) {
        var spalte = 0
        while (spalte < 7) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 7] == wer && brett[index + 14] == wer && brett[index + 21] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    zeile = 0
    while (zeile < 3) {
        var spalte = 0
        while (spalte < 4) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 8] == wer && brett[index + 16] == wer && brett[index + 24] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    zeile = 0
    while (zeile < 3) {
        var spalte = 3
        while (spalte < 7) {
            val index = 7 * zeile + spalte
            if (brett[index] == wer && brett[index + 6] == wer && brett[index + 12] == wer && brett[index + 18] == wer) {
                return true
            } else {
                spalte += 1
            }
        }

        zeile += 1
    }
    return false
}

fun start(): Int {
    while (true) {
        println("Wills du beginnen (j/n)")
        val e = readLine()
        if (e == "j") {
            return 1
        } else if (e == "n") {
            return -1
        }
        println("Falsche Eingabe")
    }

}

fun main() {
    System.setProperty("java.util.concurrent.ForkJoinPool.common.parallelism", "8")

    while (true) {
        partie()

        println("Wills du erneut spielen? (j/n)")
        while (true) {
            val e = readLine()
            if (e == "j") {
                brett.forEachIndexed{index, i -> brett[index] = 0 }
                break
            } else if (e == "n") {
                return
            }
            println("Falsche Eingabe")
        }
    }
}

private fun partie() {
    val s = schwierigkeit()
    var dran = start()
    var runde = 1
    while (true) {
        if (runde == 10) {
            if (s == Schwierigkeit.schwer) {
                s.minMax = 2
                s.monteCarlo = 50000
            }
        } else if (runde == 15) {
            if (s == Schwierigkeit.schwer) {
                s.minMax = 3
                s.monteCarlo = 10000
            } else if (s == Schwierigkeit.mittel) {
                s.minMax = 2
                s.monteCarlo = 25000
            }
        } else if (runde == 20) {
            if (s == Schwierigkeit.schwer) {
                s.minMax = 4
                s.monteCarlo = 5000
            } else if (s == Schwierigkeit.mittel) {
                s.minMax = 3
                s.monteCarlo = 5000
            }
            if (s == Schwierigkeit.leicht) {
                s.minMax = 2
                s.monteCarlo = 5000
            } else if (runde == 25) {
                if (s == Schwierigkeit.schwer) {
                    s.minMax = 5
                    s.monteCarlo = 4000
                } else if (s == Schwierigkeit.mittel) {
                    s.minMax = 4
                    s.monteCarlo = 2000
                    if (s == Schwierigkeit.leicht) {
                        s.minMax = 3
                        s.monteCarlo = 1000
                    }
                } else if (runde == 26) {
                    if (s == Schwierigkeit.schwer) {
                        s.minMax = 16
                        s.monteCarlo = 1
                    }
                } else if (runde == 27) {
                    if (s == Schwierigkeit.mittel) {
                        s.minMax = 15
                        s.monteCarlo = 1
                    }
                } else if (runde == 31) {
                    if (s == Schwierigkeit.leicht) {
                        s.minMax = 11
                        s.monteCarlo = 1
                    }
                }
            }
        }
        if (dran == -1) {
            computerZug(s, zug = runde)
            dran = 1
        } else {
            menschZug(züge = runde)
            dran = -1
        }
        println("  1 2 3 4 5 6 7  ")
        println("+---------------+")
        var zeile = 0
        while (zeile < 6) {
            var spalte = 0
            print("| ")
            while (spalte < 7) {
                print(when (brett[zeile * 7 + spalte]) {
                    0 -> "  "
                    1 -> "o "
                    else -> "x "
                })

                spalte += 1

            }

            println("|")
            zeile += 1
        }
        println("+---------------+")

        if (gewonnen(brett, 1)) {
            println("Du hast verloren")
            return
        }
        if (gewonnen(brett, -1)) {
            println("Du hast gewonnen")
            return
        }
        runde += 1
        if (runde == 42) {
            println("Es ist unentschieden")
            return
        }
    }
}

fun schwierigkeit(): Schwierigkeit {
    val size = Schwierigkeit.values().size
    println("Wähle die Schwierigkeit (1 - $size)")
    while (true) {
        val eingabe = readLine()?.toIntOrNull()

        if (eingabe == null || eingabe < 1 || eingabe > size) {
            println("Falsche Eingabe")
            continue
        }
        return Schwierigkeit.values()[eingabe - 1]
    }
}