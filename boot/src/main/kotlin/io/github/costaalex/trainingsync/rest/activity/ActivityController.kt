package io.github.costaalex.trainingsync.rest.activity

import io.github.costaalex.trainingsync.app.activity.ActivityService
import io.github.costaalex.trainingsync.app.activity.CopyActivitiesRequest
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RestController

@RestController
class ActivityController(
    private val activityService: ActivityService,
) {

    @PostMapping("/api/activities/copy")
    fun syncActivities(@RequestBody request: CopyActivitiesRequest) =
        activityService.syncActivities(request)
}
