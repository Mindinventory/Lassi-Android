import java.text.SimpleDateFormat
import java.util.*

object TimerUtils {

    fun formatTimeInMinuteSecond(millisec: Long): String {
        val d = Date(millisec)
        val df = SimpleDateFormat("mm:ss", Locale.US)
        df.timeZone = TimeZone.getTimeZone("GMT")
        return df.format(d)
    }
}