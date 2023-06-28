import com.fitness.base.BaseApplication

fun Any.i18n(res: Int): String {
    return BaseApplication.instance().resources.getString(res)
}
