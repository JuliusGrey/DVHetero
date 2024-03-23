//package CGRA.yzArch
//import CGRA.module.ModuleInfo
//import scala.collection.mutable.ListBuffer
//
//object Replace {
//  def ReplaceModule(moduleReplaced : ModuleInfo, repName : String , moduleReplacing:ModuleInfo) :ModuleInfo ={
//    val modulelist = moduleReplaced.getModuleList()
//    val modulebuf = new ListBuffer[ModuleInfo]
//    println("repName " +repName)
//    for( i <- modulelist.indices){
//      println( "i si " + i)
//      println( "name  si " + modulelist(i).getName() )
//      if(modulelist(i).getName() != repName){
//        modulebuf.append(modulelist(i))
//      }else{
//
//        require(modulelist(i).getInPortList().size == moduleReplacing.getInPortList().size)
//        require(modulelist(i).getOutPortList().size == moduleReplacing.getOutPortList().size)
//        println(moduleReplacing.getConnect().size)
//        modulebuf.append(moduleReplacing)
//      }
//    }
//    println(modulebuf(0).getName())
//    println(modulebuf(1).getName())
//    println(modulebuf(1).getConnect().size)
//    new ModuleInfo(moduleReplaced.getType() ,moduleReplaced.getName(),modulebuf.toList,moduleReplaced.getModuleInsts(),
//      moduleReplaced.getElementLists(),moduleReplaced.getInPortList(),moduleReplaced.getOutPortList(),moduleReplaced.getConnect(),moduleReplaced.getWidth())
//  }
//}
