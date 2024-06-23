package net.eneiluj.moneybuster.service

import android.content.Context
import android.util.Log
import androidx.preference.PreferenceManager
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.Worker
import androidx.work.WorkerParameters
import net.eneiluj.moneybuster.R
import net.eneiluj.moneybuster.persistence.MoneyBusterSQLiteOpenHelper
import java.util.concurrent.TimeUnit


class SyncWorker(
    val context: Context,
    workerParams: WorkerParameters,
) : Worker(context, workerParams) {

    companion object {
        private const val TAG = "SyncWorker"

        @JvmStatic
        fun submitWork(context: Context) {
            val prefs = PreferenceManager.getDefaultSharedPreferences(context)
            val intervalStr =
                prefs.getString(context.getString(R.string.pref_key_sync_interval), "15")
            val intervalMins = intervalStr?.toLongOrNull() ?: 15

            val constraints = Constraints.Builder()
                .setRequiredNetworkType(NetworkType.CONNECTED)
                .build()

            val syncWorkRequest =
                PeriodicWorkRequestBuilder<SyncWorker>(intervalMins, TimeUnit.MINUTES)
                    .setConstraints(constraints)
                    .build()

            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(TAG, ExistingPeriodicWorkPolicy.UPDATE, syncWorkRequest)
        }

        @JvmStatic
        fun cancelWork(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(TAG)
        }
    }

    override fun doWork(): Result {
        val db = MoneyBusterSQLiteOpenHelper.getInstance(context)
        Log.v(TAG, "requesting sync of all projects")

        for (proj in db.projects) {
            if (proj.isLocal) {
                Log.v(TAG, "project ${proj.remoteId} is local, not syncing")
                continue
            }
            if (isStopped) {
                Log.w(TAG, "Worker stopped, breaking")
                return Result.retry()
            }

            Log.v(TAG, "requesting sync of project ${proj.remoteId}")
            val syncTask = db.moneyBusterServerSyncHelper.scheduleSync(false, proj.id)
            // await task
            // FIXME: run all tasks in parallel?
            syncTask?.get()
            Log.v(TAG, "finished sync of project ${proj.remoteId}")
        }
        return Result.success()
    }

}