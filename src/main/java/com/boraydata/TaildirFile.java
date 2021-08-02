package com.boraydata;

import com.boraydata.exception.TaildirException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/** 文件按指定位置读取的类
 * @author bufan
 * @data 2021/7/27
 */
public class TaildirFile {

    private RandomAccessFile raf;
    private String filePath;
    private String fileName;
    private long filePointer;
    private long fileLength;

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public long getFileLength() {
        return fileLength;
    }

    public void setFileLength(long fileLength) {
        this.fileLength = fileLength;
    }

    public long getFilePointer() {
        if(raf==null)
            return filePointer;
        else {
            try {
                return raf.getFilePointer();
            } catch (IOException e) {
                throw new TaildirException("Failed to getFilePointer");
            }
        }
    }

    public void setFilePointer(long filePointer) {
        this.filePointer = filePointer;
    }

    public RandomAccessFile getRaf() {
        if(raf==null)
            throw new TaildirException(" RandomAccessFile not Found !!!!!");
        return raf;
    }

    public void setRaf(RandomAccessFile raf) {
        this.raf = raf;
    }
    public void setRaf(String filePath){
        setRaf(filePath,getFilePointer());
    }
    public void setRaf(String filePath,long pointer) {
        File file = new File(filePath);
        // 如果文件在 则继续
        if(file.exists()){
            try {
                if (this.raf==null)
                    this.raf = new RandomAccessFile(filePath, "r");
                this.raf.seek(pointer);
            } catch (IOException e) {
                throw new TaildirException("Failed to Creat RandomAccessFile",e);
            }
        }else
            throw new TaildirException("Failed to create TaildirFile , Failed is not found");
    }

    public TaildirFile(){ }
    /**
     * 使用  对象的信息来操作文件
     */
    public TaildirFile(String filePath,String fileName,long filePointer,long fileLength){
        this.filePath = filePath;
        this.fileName = fileName;
        this.filePointer = filePointer;
        this.fileLength = fileLength;
    }
    /**
     * 使用 路径来创建对象
     */
    public TaildirFile(String filePath){
        File file = new File(filePath);
        // 如果文件在 则继续
        if(file.exists()) {
            try {
                this.filePath = file.getCanonicalPath();
                this.fileName = file.getName();
                this.fileLength = file.length();
            } catch (IOException e) {
                throw new TaildirException("Failed to Creat Taildir with filePath",e);
            }
        }
    }


    public String readLine(){
        if (getFilePointer()>=getFileLength())
            throw new TaildirException("The FilePointer in the end");
        try {
            return this.raf.readLine();
        } catch (IOException e) {
            throw new TaildirException("Error ReadLine in TaildirFile ",e);
        }
    }

    public List<String> readAllLine(){
        List<String> list = new ArrayList<>();
        while (getFilePointer()<getFileLength())
            list.add(readLine());
        return list;
    }

    public void close(){
        try {
            setFilePointer(this.raf.getFilePointer());
            setFileLength(this.raf.length());
            this.raf.close();
            this.raf = null;
        } catch (IOException e) {
            throw new TaildirException("Error Close in TaildirFile ",e);
        }
    }

    //  10 -> \n   换行   LF
    //  13 -> \r   回车   CR
    public void readNexts(){
        int i = 0;
        while (true){
            try {
                 i = this.raf.read();
                if(i==-1)
                    break;
            } catch (IOException e) {
                throw new TaildirException("Error ReadNexts in TaildirFile ",e);
            }
        }
    }

    public String toString() {
        return "TaildirInfoEntity{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePointer=" + filePointer +
                ", fileLength=" + fileLength +
                '}';
    }
}
