package top.xherror.mutualhelpers

import android.util.Log
import androidx.room.*
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File

const val DATABASE_NAME="items.db"
object DateBase {
    val tag="DateBase"
    lateinit var categorydb:TinyDB
    lateinit var categoryList:ArrayList<Category>
    lateinit var myItemList:ArrayList<EntityItem>
    lateinit var db:AppDatabase
    lateinit var itemDao:ItemDao
    lateinit var remoteHelper:RemoteHelper

    fun init(name:String, version:Int){
        categoryList=ArrayList<Category>()
        db = Room.databaseBuilder(
            MyApplication.getContext(),
            AppDatabase::class.java, DATABASE_NAME
        ).allowMainThreadQueries().build()
        remoteHelper=RemoteHelper()
        updateCheck()
        itemDao = db.itemDao()

        myItemList= ArrayList(itemDao.getMyItems(person.account))

        categorydb= TinyDB(MyApplication.getContext(),"categoryList")
        if (categorydb.getListString(DEFAULT_CATEGORY).isEmpty()){
            val array=ArrayList<String>()
            array.add(DEFAULT_ATTRIBUTES)
            categorydb.putListString(DEFAULT_CATEGORY,array)
        }
        val categoryMap=categorydb.all

        for ((k,v) in categoryMap){
            val list=categorydb.getListString(k)
            val category=Category(k,list)
            categoryList.add(category)
        }
        Log.d(tag,categoryList.toString())
    }

    fun addItem( item: EntityItem,file:File?,timeStamp: Long){
        //TODO:modify remoteHelper func call
        setTimeStamp(timeStamp)
        remoteHelper.addItem(item)
        file?.let { remoteHelper.addImage(file) }
        /*
        val category=getCategory(item.category)
        category?.notifyItemAdd(item)
        notifyMyItemAdd(item)

         */
    }

    fun updateCheck(){
        remoteHelper.updateCheck(settingdb.getLong("timeStamp"))
    }

    fun updatePerson(person: Person,file:File?){
        //TODO:insert person
        persondb.putListString(person.account,person.toList())
        if (rememberdb.getString("rememberAccount").isNotEmpty()){
            rememberdb.putListString(person.account,person.toList())
        }
        file?.let { remoteHelper.addAvatar(file) }
    }

    fun updateItem(item: EntityItem){
        itemDao.updateItems(item)
    }

    //TODO:delete image
    fun deleteItem(item: EntityItem){
        itemDao.deleteItems(item)
        remoteHelper.deleteItem(item)
        myItemList.remove(item)
        val category=getCategory(item.category)
        category?.notifyItemDelete(item)
        val timeStamp=System.currentTimeMillis()/1000
        setTimeStamp(timeStamp)
    }

    fun getAll()=ArrayList<EntityItem>(itemDao.getAll())

    fun getSpecialCategory(categoryName:String)=ArrayList<EntityItem>(itemDao.getSpecialCategory(categoryName))

    fun getCategoryNameList():ArrayList<String>{
        val array=ArrayList<String>()
        categoryList.onEach {
            array.add(it.name)
        }
        return array
    }

    fun getCategory(name:String)=categoryList.find { it.name==name }

    fun getMyItemSearchResult(searchString:String):ArrayList<EntityItem>{
        //TODO:模糊搜索,ES,轻量NN
        val array=ArrayList<EntityItem>()
        for (index :Int in myItemList.lastIndex downTo 0) {
            if (searchString in myItemList[index].attributes||searchString in myItemList[index].name||searchString in myItemList[index].description) array.add(myItemList[index])
        }
        return  array
    }

    fun notifyMyItemAdd(addEntityItem:EntityItem){
        myItemList.add(addEntityItem)
    }

    fun notifyMyItemDelete(deleteEntityItem: EntityItem){
        myItemList.remove(deleteEntityItem)
    }

    fun setTimeStamp(time:Long){
        settingdb.putLong("timeStamp",time)
    }

}

@androidx.room.Database(entities = [EntityItem::class], version = 1,exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun itemDao(): ItemDao
}

const val CHOOSE_GALLERY=0
const val CHOOSE_CAMERA=1
@androidx.room.Entity
data class EntityItem (
    @PrimaryKey var id: Int,
    @ColumnInfo(name = "name") var name: String,
    @ColumnInfo(name = "category") var category: String,
    @ColumnInfo(name = "location") var location:String,
    @ColumnInfo(name = "time") var time:String,
    @ColumnInfo(name = "imagePath") var imageName:String,
    @ColumnInfo(name = "imageWidth") var imageWidth:Int,
    @ColumnInfo(name = "imageHeight") var imageHeight:Int,
    @ColumnInfo(name = "phone") var phone:String,
    @ColumnInfo(name = "ownerAccount") var ownerAccount:String,
    @ColumnInfo(name = "attributes") var attributes:String,
    @ColumnInfo(name = "description") var description:String,
    @ColumnInfo(name = "comments") var comments:String
)

@androidx.room.Entity
data class TestItem (
    @PrimaryKey(autoGenerate = true) var id: Int=-1,
    @ColumnInfo(name = "name") var name: String
)

@Dao
interface ItemDao {
    @Insert
    fun insertItems(vararg items: EntityItem)

    @Delete
    fun deleteItems(vararg items: EntityItem)

    @Update
    fun updateItems(vararg items: EntityItem)

    @Query("SELECT * FROM EntityItem WHERE category = :category")
    fun getSpecialCategory(category:String): List<EntityItem>

    @Query("SELECT * FROM EntityItem")
    fun getAll(): List<EntityItem>

    @Query("SELECT * FROM EntityItem WHERE ownerAccount = :ownerAccount")
    fun getMyItems(ownerAccount: String): List<EntityItem>
}