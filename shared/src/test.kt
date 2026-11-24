package login.dinosamazingguistuff

import login.dinosamazingguistuff.resources.Res

object test {
    fun doStuff():List<String>?{
        var output = Shell.run("dir /B")
        println ("Code: ${output.exitCode}\nStdout: ${output.stdout}\nStderr: ${output.stderr}")
        if(output.exitCode != 0){
            return null
        }
        var out=output.stdout.lines().filter { it.isNotEmpty() }
        //out = out.subList(5,out.size-3)
        println("Directories: ")
        for (i in out.indices){
            println("$i: ${out[i]}")
        }
        return out
    }
}

fun main(args: Array<String>) {
    var x=test.doStuff()?:"FAILED"
    print(x)

}