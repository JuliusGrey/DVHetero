package CGRA.module

import CGRA.parameter.Param.dataW

import scala.collection.mutable.{ArrayBuffer, ListBuffer, Map}
import java.io.FileWriter
object DumpInfo {


  val moduleList : Map[String , Tuple2[ModuleInfo,Int]]= Map()

  def output(filename: String): Unit = {
    var depth = moduleList.values.map(_._2).max
    val file: FileWriter = new FileWriter(filename, false)
    for (k <- 0 to depth) {
      val i = depth - k
      file.write("// the level is" + i + "\n")

      moduleList.map {
        case (devicename, module) => if (module._2 == i) {

          file.write("val " + module._1.getName() + " = new ModuleInfo(\n")

          file.write("    \"" + module._1.getType() + "\",\n")

          file.write("    \"" + module._1.getName() + "\",\n")



          file.write(" " * 4 + "List(\n")

          module._1.getModuleList().map{
            moduleIns => {
              file.write(" " * 8  + moduleIns.getName() +  "\n")
            }
          }
          file.write(" " * 4 + "),\n")


          file.write(" " * 4 + module._1.getModuleInsts().toString + ",\n")













          file.write(" " * 4 + "Map(\n")
          module._1.getElementLists().map {
            case (type_, elelist) => {
              file.write(" " * 8 + type_ + " -> List(\n")

              // elelist.map( ele =>{
              //     if(ele != elelist.last)
              //         {file.write(" "* 12 + ele + ", \n")}
              //     else
              //         {file.write(" "* 12 + ele + " \n")}
              // })

              for (i <- 0 until elelist.size) {
                if (i == elelist.size - 1) {
                  file.write(" " * 12 + elelist(i) + " \n")
                } else {
                  file.write(" " * 12 + elelist(i) + ", \n")
                }
              }


              if (module._1.getElementLists().last != (type_, elelist)) {
                file.write(" " * 8 + "),\n")
              }
              else {
                file.write(" " * 8 + ")\n")
              }
            }
          }
          file.write(" " * 4 + "),\n")

          file.write(" " * 4 + "List(")
          module._1.getInPortList().map(
            inputname =>
              if (inputname != module._1.getInPortList.last) {
                file.write("\"" + inputname + "\",")
              }
              else {
                file.write("\"" + inputname + "\"")
              }
          )
          file.write("),\n")

          file.write(" " * 4 + "List(")
          module._1.getOutPortList().map(
            outputname =>
              if (outputname != module._1.getOutPortList.last) {
                file.write("\"" + outputname + "\",")
              }
              else {
                file.write("\"" + outputname + "\"")
              }
          )
          file.write("),\n")

          file.write(" " * 4 + "List(\n")
          module._1.getConnect().map(
            connecttup => {
              file.write(" " * 8 + "(" + connecttup._1 + ",List(\n")
              connecttup._2.map(
                sink =>
                  if (sink != connecttup._2.last)
                    file.write(" " * 12 + sink + ",\n")
                  else
                    file.write(" " * 12 + sink + "\n")
              )
              if (connecttup != module._1.getConnect().last) {
                file.write(" " * 8 + ")),\n")
              }
              else {
                file.write(" " * 8 + "))\n")
              }
            }
          )
          file.write(" " * 4 + "),\n")

          file.write(" " * 4 + dataW + "\n")

          file.write(")" + "\n" * 3)


        }
      }
    }
    file.close
  }


  def infooutput(filename: String, module: ModuleInfo): Unit = {
    moduleList.clear
    val namelist = new ArrayBuffer[String]
    moduleList += (module.getName() -> (module, 0))
    namelist.append(module.getName())
    var continue = true
    while (continue) {
      continue = false
      val name2list = namelist.toList
      namelist.clear
      name2list.map(
        ins => {
          val moduletup = moduleList(ins)
          if (!moduletup._1.getModuleInsts().isEmpty) {
            moduletup._1.getModuleInsts().map {
              i => {
                if (!moduleList.contains(moduletup._1.getModuleList()(i).getName())) {
                  moduleList += ((moduletup._1.getModuleList()(i).getName() -> (moduletup._1.getModuleList()(i), moduletup._2 + 1)))
                  namelist.append(moduletup._1.getModuleList()(i).getName())
                  continue = true
                }
              }





            }
          }
        }
      )
    }
    output(filename)
  }
}
