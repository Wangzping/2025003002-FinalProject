package com.example.campushub.data.repository

import com.example.campushub.data.dao.CheckInDao
import com.example.campushub.data.entity.CheckInCodeEntity
import com.example.campushub.data.entity.CheckInRecordEntity
import kotlin.random.Random

class CheckInRepository(private val checkInDao: CheckInDao) {

    companion object {
        private const val CODE_EXPIRY_MINUTES = 5L
        private const val DEFAULT_CREDIT_HOURS = 2
    }

    /** 生成一个 6 位签到码，存入数据库，5 分钟后过期 */
    suspend fun generateCode(activityId: Int): String {
        checkInDao.deactivateExpiredCodes(System.currentTimeMillis())

        // 生成 6 位随机数字码
        val code = (100000..999999).random().toString()

        val entity = CheckInCodeEntity(
            activityId = activityId,
            code = code,
            expiresAt = System.currentTimeMillis() + CODE_EXPIRY_MINUTES * 60 * 1000
        )
        checkInDao.insertCode(entity)
        return code
    }

    /** 验证签到码是否有效 */
    suspend fun verifyCode(activityId: Int, inputCode: String): Boolean {
        val activeCode = checkInDao.getActiveCode(activityId, System.currentTimeMillis())
        return activeCode != null && activeCode.code == inputCode
    }

    /** 参与者签到 */
    suspend fun checkIn(activityId: Int, totalCreditHours: Int): Boolean {
        if (checkInDao.hasCheckedIn(activityId)) return false

        val record = CheckInRecordEntity(
            activityId = activityId,
            duration = totalCreditHours * 60 // 按学分换算为分钟（1 学分 = 60 分钟）
        )
        checkInDao.insertRecord(record)
        return true
    }

    /** 检查是否已签到 */
    suspend fun hasCheckedIn(activityId: Int): Boolean {
        return checkInDao.hasCheckedIn(activityId)
    }

    /** 获取总签到次数 */
    suspend fun getTotalCheckIns(): Int {
        return checkInDao.getTotalCheckIns()
    }

    /** 获取总学时（第二课堂学分统计） */
    suspend fun getTotalCreditHours(): Int {
        val totalMinutes = checkInDao.getTotalDuration()
        return totalMinutes / 60 // 分钟转学时
    }
}
