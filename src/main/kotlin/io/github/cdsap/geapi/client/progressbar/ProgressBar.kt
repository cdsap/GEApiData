package io.github.cdsap.geapi.client.progressbar

class ProgressBar {
    private var progress: StringBuilder? = null

    init {
        init()
    }

    fun update(
        done: Int,
        total: Int,
    ) {
        var counter = done
        val workchars = charArrayOf('|', '/', '-', '\\')
        val format = "\r%3d%% %s %c"
        val percent = ++counter * 100 / total
        var extrachars = percent / 2 - progress!!.length
        while (extrachars-- > 0) {
            progress!!.append('#')
        }
        System.out.printf(
            format,
            percent,
            progress,
            workchars[done % workchars.size],
        )
        if (done == total) {
            System.out.flush()
            println()
            init()
        }
    }

    private fun init() {
        progress = StringBuilder(60)
    }
}
