package root

/**
 * Created by gsingh on 22/4/2020 AD
 */
object Example  extends App {

  val list = List(("a", 1),("b", 2),("c", 3))
  val map = list.map(i => i._1 -> i._2).toMap
  println(map.getOrElse("d", false))
}
