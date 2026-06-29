package com.example.campushub.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.campushub.model.CampusActivity
import com.example.campushub.ui.theme.*

@Composable
fun ActivityCard(
    activity: CampusActivity,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val categoryColor = when (activity.category) {
        "学术竞赛" -> CategoryCompetition
        "学术讲座" -> CategoryLecture
        "文艺活动" -> CategoryArts
        "体育赛事" -> CategorySports
        "公益活动" -> CategoryCharity
        else -> PrimaryLight
    }

    Card(
        modifier = modifier.fillMaxWidth().clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column {
            // Top color bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .background(categoryColor)
            )

            Column(modifier = Modifier.padding(16.dp)) {
                // Title row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = activity.title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.weight(1f),
                        maxLines = 1, overflow = TextOverflow.Ellipsis
                    )
                    Surface(
                        color = categoryColor.copy(alpha = 0.15f),
                        shape = RoundedCornerShape(6.dp)
                    ) {
                        Text(
                            text = activity.category,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp),
                            style = MaterialTheme.typography.labelSmall,
                            color = categoryColor,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    text = activity.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 2, overflow = TextOverflow.Ellipsis
                )

                Spacer(Modifier.height(12.dp))

                // Info row
                Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                    InfoChip("📅", activity.date)
                    InfoChip("📍", activity.location)
                }

                Spacer(Modifier.height(8.dp))

                Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        InfoChip("👤", activity.organizer)
                    }

                    // Status badge
                    val (badgeColor, badgeText) = when {
                        activity.isRegistered -> Pair(MaterialTheme.colorScheme.primary, "已报名")
                        activity.maxParticipants > 0 && activity.currentParticipants >= activity.maxParticipants ->
                            Pair(MaterialTheme.colorScheme.error, "已满")
                        else -> Pair(MaterialTheme.colorScheme.secondary, "可报名")
                    }
                    Surface(color = badgeColor.copy(alpha = 0.15f), shape = RoundedCornerShape(6.dp)) {
                        Text(
                            text = badgeText,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = badgeColor
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoChip(icon: String, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(text = icon, style = MaterialTheme.typography.bodySmall)
        Spacer(Modifier.width(4.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1, overflow = TextOverflow.Ellipsis
        )
    }
}
