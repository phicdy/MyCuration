package com.phicdy.mycuration.domain.setting

data class SettingInitialData(
        val updateIntervalHourItems: Array<String>,
        val updateIntervalStringItems: Array<String>,
        val themeItems: Array<String>,
        val themeStringItems: Array<String>,
        val allReadBehaviorItems: Array<String>,
        val allReadBehaviorStringItems: Array<String>,
        val launchTabItems: Array<String>,
        val launchTabStringItems: Array<String>,
        val swipeDirectionItems: Array<String>,
        val swipeDirectionStringItems: Array<String>
) {
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as SettingInitialData

        if (!updateIntervalHourItems.contentEquals(other.updateIntervalHourItems)) return false
        if (!updateIntervalStringItems.contentEquals(other.updateIntervalStringItems)) return false
        if (!themeItems.contentEquals(other.themeItems)) return false
        if (!themeStringItems.contentEquals(other.themeStringItems)) return false
        if (!allReadBehaviorItems.contentEquals(other.allReadBehaviorItems)) return false
        if (!allReadBehaviorStringItems.contentEquals(other.allReadBehaviorStringItems)) return false
        if (!launchTabItems.contentEquals(other.launchTabItems)) return false
        if (!launchTabStringItems.contentEquals(other.launchTabStringItems)) return false
        if (!swipeDirectionItems.contentEquals(other.swipeDirectionItems)) return false
        if (!swipeDirectionStringItems.contentEquals(other.swipeDirectionStringItems)) return false

        return true
    }

    override fun hashCode(): Int {
        var result = updateIntervalHourItems.contentHashCode()
        result = 31 * result + updateIntervalStringItems.contentHashCode()
        result = 31 * result + themeItems.contentHashCode()
        result = 31 * result + themeStringItems.contentHashCode()
        result = 31 * result + allReadBehaviorItems.contentHashCode()
        result = 31 * result + allReadBehaviorStringItems.contentHashCode()
        result = 31 * result + launchTabItems.contentHashCode()
        result = 31 * result + launchTabStringItems.contentHashCode()
        result = 31 * result + swipeDirectionItems.contentHashCode()
        result = 31 * result + swipeDirectionStringItems.contentHashCode()
        return result
    }
}