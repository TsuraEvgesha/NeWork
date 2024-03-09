package ru.netology.nework.app.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint
import ru.netology.nework.R
import ru.netology.nework.app.auth.AppAuth
import ru.netology.nework.app.dto.Event
import ru.netology.nework.app.dto.Post
import ru.netology.nework.app.dto.PushMessage
import ru.netology.nework.app.service.Action.ERROR
import javax.inject.Inject
import kotlin.random.Random

@AndroidEntryPoint
class FCMService : FirebaseMessagingService() {
    private val content = "content"
    private val channelId = "remote"
    private val action = "action"
    private val gson = Gson()

    @Inject
    lateinit var appAuth: AppAuth

    override fun onCreate() {
        super.onCreate()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = getString(R.string.channel_remote_name)
            val descriptionText = getString(R.string.channel_remote_description)
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(channelId, name, importance).apply {
                description = descriptionText
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(channel)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        try {
            val body = gson.fromJson(message.data[content], PushMessage::class.java)
            when (body.recipientId) {
                appAuth.authStateFlow.value.id, null -> {
                    handlePush(body)
                }

                else -> appAuth.sendPushToken()
            }
            println(message.data[content])
        } catch (e: Exception) {
            e.printStackTrace()
        }
        message.data[action]?.let {
            when (Action.getValidAction(it)) {
                Action.LIKE_POST -> handleLike(
                    gson.fromJson(
                        message.data[content],
                        Post::class.java
                    )
                )

                Action.NEW_POST -> handleNewPost(
                    gson.fromJson(
                        message.data[content],
                        Post::class.java
                    )

                )

                Action.LIKE_EVENT -> handleLikeEvent(
                    gson.fromJson(
                        message.data[content],
                        Event::class.java
                    )
                )

                Action.NEW_EVENT -> handleNewEvent(
                    gson.fromJson(
                        message.data[content],
                        Event::class.java
                    )

                )

                ERROR -> println("ERROR_PUSH")
            }
        }
    }

    override fun onNewToken(token: String) {
        appAuth.sendPushToken(token)
        println("token $token")
    }

    private fun handlePush(push: PushMessage) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentText(push.content)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handleLike(content: Post) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked,
                    content.likeOwnerIds,
                    content.author,
                )
            )

            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handleLikeEvent(content: Event) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(
                getString(
                    R.string.notification_user_liked_event,
                    content.likeOwnerIds,
                    content.author,
                )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handleNewPost(content: Post) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(
                getString(
                    R.string.notification_new_post,
                    content.author
                )
            )
            .setContentText(content.published)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.content)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }

    private fun handleNewEvent(content: Event) {
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.mipmap.ic_launcher_round)
            .setContentTitle(
                getString(
                    R.string.notification_new_post,
                    content.author
                )
            )
            .setContentText(content.published)
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(content.content)
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        NotificationManagerCompat.from(this)
            .notify(Random.nextInt(100_000), notification)
    }
}


enum class Action {
    LIKE_POST, NEW_POST, LIKE_EVENT, NEW_EVENT, ERROR;

    companion object {
        fun getValidAction(action: String): Action {
            return try {
                valueOf(action)
            } catch (exception: IllegalArgumentException) {
                ERROR
            }
        }
    }
}