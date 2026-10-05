package com.example.wherewegoing.data.local

import androidx.room.*

@Entity(tableName="user_auth_identity",primaryKeys=["user_id","project"],
 foreignKeys=[ForeignKey(entity=AppUserEntity::class,parentColumns=["user_id"],childColumns=["user_id"],onDelete=ForeignKey.RESTRICT)],
 indices=[Index(value=["project","auth_user_id"],unique=true)])
data class UserAuthIdentity(@ColumnInfo(name="user_id") val userId:String,val project:String,
 @ColumnInfo(name="auth_user_id") val authUserId:String)
@Dao interface UserAuthIdentityDao {
 @Query("SELECT * FROM user_auth_identity WHERE user_id=:user AND project=:project") suspend fun find(user:String,project:String):UserAuthIdentity?
 @Insert suspend fun insert(identity:UserAuthIdentity)
}
