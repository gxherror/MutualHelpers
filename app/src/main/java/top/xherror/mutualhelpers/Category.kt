package top.xherror.mutualhelpers

class Category(var name:String="",var array:ArrayList<String>) {
    var imagePath:String=""
    var attributes=ArrayList<String>()
    init {
        array.onEachIndexed { index, s ->
            if (index==0){
                imagePath=s
            }else{
                attributes.add(s)
            }
        }
    }
    fun getAttrString():String{
        var str=""
        attributes.onEachIndexed { index, s ->
            str=str.plus(s+"\n")
        }
        return str
    }
}