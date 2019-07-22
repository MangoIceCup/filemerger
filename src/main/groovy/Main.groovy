import java.nio.charset.Charset
import java.nio.file.Files
import java.nio.file.Paths

assert args

def dictFiles = [] as File[]
File rootPath = Paths.get(args[0]).toFile()

def MergeFile(OutputStream outputStream, File sourceFile) {
    int data
    //region Description
    FileInputStream fileInputStream = new FileInputStream(sourceFile)
    BufferedInputStream bufferedInputStream=new BufferedInputStream(fileInputStream)
    //endregion
    data = bufferedInputStream.read()
    while (data != -1) {
        outputStream.write(data)
        data = bufferedInputStream.read()
    }
    bufferedInputStream.close()
    if (outputStream instanceof TrimSpecialEndOutputStream) {
        outputStream.trimSpecialEndOrPushEndDataNow()
    }
}

def RecursiveFind(File root, String extionsion) {
    def files = []
    root.eachFile {
        if (it.file && it.name.endsWith(extionsion)) {
            files << it
        }
    }
    root.eachFile {
        if (!it.file) {
            def fs = RecursiveFind(it, extionsion)
            files.addAll(fs)
        }
    }
    return files
}

dictFiles = RecursiveFind(rootPath, ".txt")
def dictFileArray = dictFiles.toArray(new File[0])
Arrays.sort(dictFileArray, {
    a, b -> (int) (Files.size(a.toPath()) - Files.size(b.toPath()))
})
File mergedOutput = Paths.get(args[1]).toFile()
Files.deleteIfExists(mergedOutput.toPath())
Files.createFile(mergedOutput.toPath())
FileOutputStream fileOutputStream = new FileOutputStream(mergedOutput)
BufferedOutputStream bufferedOutputStream = new BufferedOutputStream(fileOutputStream)
TrimSpecialEndOutputStream trimSpecialEndOutputStream_cr = new TrimSpecialEndOutputStream(bufferedOutputStream, "\r".getBytes(Charset.forName("UTF-8")))
TrimSpecialEndOutputStream trimSpecialEndOutputStream_lf = new TrimSpecialEndOutputStream(trimSpecialEndOutputStream_cr, "\n".getBytes(Charset.forName("UTF-8")))
dictFileArray[0..-2].each {
    MergeFile(trimSpecialEndOutputStream_lf, it)
    bufferedOutputStream.write("\n".getBytes("UTF-8"))
}
MergeFile(trimSpecialEndOutputStream_lf, dictFileArray[-1])

trimSpecialEndOutputStream_lf.close()
