package com.example.mobilecomputing
import android.content.Context
import androidx.room.*

@Entity(tableName = "reminders")
data class ReminderData(
    @PrimaryKey(autoGenerate = true) val uid : Int?,
    @ColumnInfo(name = "title") var title : String,
    @ColumnInfo(name = "message") var message : String,
    @ColumnInfo(name = "location_x") var locationX : String,
    @ColumnInfo(name = "location_y") var locationY : String,
    @ColumnInfo(name = "reminder_time") var reminderTime : String,
    @ColumnInfo(name = "creation_time") var creationTime : String,
    @ColumnInfo(name = "creator_id") var creatorId : String,
    @ColumnInfo(name = "reminder_seen") var reminderSeen : Boolean
)

@Dao
interface ReminderDao {

    @Query("SELECT * FROM reminders WHERE creator_id LIKE :username")
    fun getRemindersForUser(username: String): List<ReminderData>

    @Query("SELECT * FROM reminders WHERE uid = :id")
    fun getReminder(id: Int): ReminderData

    @Insert
    fun insert(reminderData: ReminderData)

    @Update
    fun update(reminderData: ReminderData)

    @Query("DELETE FROM reminders WHERE uid LIKE :id")
    fun remove(id: Int)
}

@Database(entities = [ReminderData::class], version = 1)
abstract class ReminderDatabase : RoomDatabase() {
    abstract fun reminderDao() : ReminderDao
}

public fun getReminderDb(context: Context) : ReminderDatabase {
    return Room.databaseBuilder(context,
        ReminderDatabase::class.java, "reminder-database").build()
}