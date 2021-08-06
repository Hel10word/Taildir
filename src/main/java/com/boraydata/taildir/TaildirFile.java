package com.boraydata.taildir;



import com.boraydata.taildir.exception.TaildirException;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;

/** 自定义的 TaildirFile 类  ，  使用Java的  RandomAccessFile ，并通过 Pointer 在指定位置读取文件信息
 * @author bufan
 * @data 2021/7/27
 *
 */
public class TaildirFile {

    // 一个 RandomAccessFile 对象
    private RandomAccessFile raf;
    // 文件的 绝对路径   作为文件的唯一表示
    private String filePath;
    // 文件名
    private String fileName;
    // 文件的 坐标 ， @raf 会从 该位置开始读取文件 默认会从文件头开始读取
    private Long filePointer = 0L;

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
        if(this.raf != null)
            try {
                return this.raf.length();
            } catch (IOException e) {
                throw new TaildirException("Error in getFileLength",e);
            }
        throw new TaildirException("Error in getFileLength");
    }

    public void setFilePointer(long filePointer) {
        this.filePointer = filePointer;
    }

    // 实时返回当前的 pointer
    public long getFilePointer() {
        if(raf == null)
            return this.filePointer;
        else {
            try {
                return raf.getFilePointer();
            } catch (IOException e) {
                throw new TaildirException("Failed to getFilePointer");
            }
        }
    }
    /**
     * 使用 路径来创建对象
     */
    public TaildirFile(String filePath){
        setRaf(filePath,0);
    }
    /**
     *  创建对象前  指定起始坐标
     */
    public TaildirFile(String filePath,long filePointer){
        setRaf(filePath,filePointer);
    }

    // 用来 更新 raf 的 读取位置
    public void setRafPointer(long pointer) {
        if (this.raf != null && pointer > 0 && pointer <= getFileLength())
            try {
                this.raf.seek(pointer);
            } catch (IOException e) {
                throw new TaildirException("Error in setRafPointer ", e);
            }
    }

    public void setRaf(String filePath){
        setRaf(filePath,getFilePointer());
    }
    // 初始化 RandomAccessFile 对象    并设置  指针位置
    public void setRaf(String filePath,long pointer) {
        File file = new File(filePath);
        // 如果文件在 则继续
        if(file.exists())
            try {
                this.filePath = file.getCanonicalPath();
                this.fileName = file.getName();
                this.raf = new RandomAccessFile(filePath, "r");
                if(pointer >= 0 && pointer <= this.raf.length())
                    setRafPointer(pointer);
                else
                    throw new TaildirException("Failed to set RandomAccessFile Pointer");
            } catch (IOException e) {
                throw new TaildirException("Failed to Creat RandomAccessFile",e);
            }
        else
            throw new TaildirException("Failed to create TaildirFile , Failed is not found");
    }

    public RandomAccessFile getRaf() {
        if(raf==null)
            throw new TaildirException("RandomAccessFile not Found !!!!!");
        return raf;
    }

    // 从 指定位置 读取一行字符
    public String readLine(){
        try {
            setFilePointer(this.raf.getFilePointer());
            return this.raf.readLine();
        } catch (IOException e) {
            throw new TaildirException("Error ReadLine in TaildirFile ",e);
        }
    }

    // 从 指定位子  读取到末尾为止的 所有行
    public List<String> readAllLine(){
        List<String> list = new ArrayList<>();
        while (getFilePointer()<getFileLength())
            list.add(readLine());
        close();
        return list;
    }
    public boolean hasNext(){
        if(this.raf == null)
            return false;

        return getFilePointer() >= 0 && getFilePointer() < getFileLength();
    }


    public void close(){
        try {
            if (this.raf == null)
                return;
            // 由于 需要清空 raf 对象    在关闭时  保存最后的信息
            setFilePointer(this.raf.getFilePointer());
            this.raf.close();
            this.raf = null;
        } catch (IOException e) {
            throw new TaildirException("Error Close in TaildirFile ",e);
        }
    }

    //  10 -> \n   换行   LF
    //  13 -> \r   回车   CR
    public int readNexts(){
        try {
            return this.raf.read();
        } catch (IOException e) {
            throw new TaildirException("Error ReadNexts in TaildirFile ",e);
        }
    }

    public String toString() {
        return "TaildirInfoEntity{" +
                "filePath='" + filePath + '\'' +
                ", fileName='" + fileName + '\'' +
                ", filePointer=" + filePointer +
                '}';
    }
}
