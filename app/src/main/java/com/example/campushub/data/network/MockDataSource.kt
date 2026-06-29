package com.example.campushub.data.network

import com.example.campushub.data.entity.ActivityEntity

object MockDataSource {
    private fun parseDateTime(date: String, time: String): Long {
        return try {
            val timeStr = time.split("-")[0].trim()
            val dateTimeStr = "$date $timeStr"
            val format = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm", java.util.Locale.getDefault())
            format.parse(dateTimeStr)?.time ?: 0
        } catch (e: Exception) {
            0
        }
    }

    private val activities = listOf(
        ActivityEntity(
            id = 1, title = "秋季校园音乐节",
            description = "一年一度的校园音乐节即将开幕！本次音乐节邀请了多位校园乐队和歌手现场表演，还有精彩的互动环节和美食摊位。欢迎全校师生参加，一起享受音乐的魅力！",
            date = "2026-10-15", time = "18:00-21:00",
            location = "学校大礼堂", organizer = "学生会文艺部",
            category = "文艺活动", maxParticipants = 500, currentParticipants = 320,
            imageUrl = "/placeholder_music.jpg",
            startTime = parseDateTime("2026-10-15", "18:00")
        ),
        ActivityEntity(
            id = 2, title = "计算机编程大赛",
            description = "面向全校学生的编程竞赛，分为算法组和应用开发组。参赛者需要在规定时间内完成编程题目或开发一个小型应用。优胜者将获得证书和奖金。",
            date = "2026-11-01", time = "09:00-17:00",
            location = "信息学院实验楼A201", organizer = "计算机协会",
            category = "学术竞赛", maxParticipants = 100, currentParticipants = 65,
            imageUrl = "/placeholder_code.jpg",
            startTime = parseDateTime("2026-11-01", "09:00")
        ),
        ActivityEntity(
            id = 3, title = "校园马拉松",
            description = "健康校园，奔跑青春！本次校园马拉松设5公里和10公里两个组别，路线环绕校园主要景点。完赛者均可获得纪念奖牌，各组前10名另有丰厚奖品。",
            date = "2026-10-28", time = "07:00-11:00",
            location = "学校田径场", organizer = "体育部",
            category = "体育赛事", maxParticipants = 300, currentParticipants = 158,
            imageUrl = "/placeholder_run.jpg",
            startTime = parseDateTime("2026-10-28", "07:00")
        ),
        ActivityEntity(
            id = 4, title = "摄影技巧分享会",
            description = "邀请资深摄影师分享手机摄影和人像摄影的技巧，内容包括构图、光线运用、后期处理等。请自带手机或相机，现场有实践环节。",
            date = "2026-10-20", time = "14:00-16:00",
            location = "艺术学院三楼会议室", organizer = "摄影社",
            category = "学术讲座", maxParticipants = 50, currentParticipants = 45,
            imageUrl = "/placeholder_camera.jpg",
            startTime = parseDateTime("2026-10-20", "14:00")
        ),
        ActivityEntity(
            id = 5, title = "爱心义卖活动",
            description = "奉献一份爱心，温暖整个校园。本次义卖所得将全部捐赠给山区小学。欢迎同学们捐赠闲置物品或购买义卖品，也可报名成为志愿者。",
            date = "2026-11-05", time = "10:00-17:00",
            location = "学校图书馆前广场", organizer = "青年志愿者协会",
            category = "公益活动", maxParticipants = 200, currentParticipants = 87,
            imageUrl = "/placeholder_charity.jpg",
            startTime = parseDateTime("2026-11-05", "10:00")
        ),
        ActivityEntity(
            id = 6, title = "英语角-感恩节特别活动",
            description = "本期英语角以感恩节为主题，通过讨论、游戏和分享，提高英语口语能力，了解西方文化。活动全程英语交流，有外教参与指导。",
            date = "2026-11-22", time = "19:00-20:30",
            location = "外国语学院咖啡厅", organizer = "英语爱好者协会",
            category = "学术讲座", maxParticipants = 40, currentParticipants = 38,
            imageUrl = "/placeholder_english.jpg",
            startTime = parseDateTime("2026-11-22", "19:00")
        ),
        ActivityEntity(
            id = 7, title = "校园辩论赛",
            description = "辩以明理，论以求真！本届校园辩论赛面向所有院系开放报名，采用淘汰赛制，设冠亚季军及最佳辩手奖。",
            date = "2026-11-10", time = "14:00-18:00",
            location = "行政楼报告厅", organizer = "演讲与辩论协会",
            category = "学术竞赛", maxParticipants = 80, currentParticipants = 52,
            imageUrl = "/placeholder_debate.jpg",
            startTime = parseDateTime("2026-11-10", "14:00")
        ),
        ActivityEntity(
            id = 8, title = "周末露天电影放映",
            description = "本周放映经典科幻电影《星际穿越》。现场提供零食和饮料，欢迎同学们带上朋友和毯子，在星空下享受电影时光。",
            date = "2026-10-19", time = "19:30-22:00",
            location = "学校东区草坪", organizer = "学生会宣传部",
            category = "文艺活动", maxParticipants = 200, currentParticipants = 175,
            imageUrl = "/placeholder_movie.jpg",
            startTime = parseDateTime("2026-10-19", "19:30")
        ),
        ActivityEntity(
            id = 9, title = "简历撰写与面试技巧工作坊",
            description = "就业指导中心特邀企业HR分享简历撰写技巧和面试注意事项。内容包括简历模板分析、常见面试问题解答和模拟面试环节。",
            date = "2026-10-25", time = "15:00-17:00",
            location = "就业指导中心多功能厅", organizer = "就业指导中心",
            category = "学术讲座", maxParticipants = 60, currentParticipants = 60,
            imageUrl = "/placeholder_interview.jpg",
            startTime = parseDateTime("2026-10-25", "15:00")
        ),
        ActivityEntity(
            id = 10, title = "校园篮球3v3挑战赛",
            description = "热血篮球，等你来战！3v3半场篮球赛，自由组队报名，设男子组和女子组。冠军队伍将获得运动装备礼包和奖杯。",
            date = "2026-10-22", time = "09:00-17:00",
            location = "室外篮球场", organizer = "篮球协会",
            category = "体育赛事", maxParticipants = 48, currentParticipants = 36,
            imageUrl = "/placeholder_basketball.jpg",
            startTime = parseDateTime("2026-10-22", "09:00")
        ),
        ActivityEntity(
            id = 11, title = "手工DIY-陶艺体验",
            description = "感受陶艺魅力，亲手制作属于自己的陶瓷作品。专业老师指导，无需经验。作品可烧制后带走，是送朋友或自留的绝佳纪念品。",
            date = "2026-11-08", time = "10:00-12:00",
            location = "陶艺工作室（艺术学院一楼）", organizer = "手工艺社",
            category = "文艺活动", maxParticipants = 30, currentParticipants = 28,
            imageUrl = "/placeholder_pottery.jpg",
            startTime = parseDateTime("2026-11-08", "10:00")
        ),
        ActivityEntity(
            id = 12, title = "校园二手书市",
            description = "学期中二手书交换活动。你可以出售不再使用的教材和书籍，也可以低价购买需要的书籍。支持以书换书，减少浪费，绿色环保。",
            date = "2026-10-29", time = "09:00-16:00",
            location = "教学楼一楼大厅", organizer = "绿色环保协会",
            category = "公益活动", maxParticipants = 0, currentParticipants = 0,
            imageUrl = "/placeholder_books.jpg"
        )
    )

    fun getActivities(): List<ActivityEntity> = activities

    fun getActivityById(id: Int): ActivityEntity? = activities.find { it.id == id }

    fun searchActivities(query: String): List<ActivityEntity> {
        if (query.isBlank()) return activities
        val q = query.lowercase()
        return activities.filter {
            it.title.lowercase().contains(q) ||
            it.description.lowercase().contains(q) ||
            it.category.lowercase().contains(q) ||
            it.location.lowercase().contains(q)
        }
    }

    fun getActivitiesByCategory(category: String): List<ActivityEntity> {
        return if (category == "all") activities
        else activities.filter { it.category == category }
    }

    fun getNextId(): Int = (activities.maxOfOrNull { it.id } ?: 0) + 1
}
