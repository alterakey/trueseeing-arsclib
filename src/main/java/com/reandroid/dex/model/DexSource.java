/*
 *  Copyright (C) 2022 github.com/REAndroid
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.reandroid.dex.model;

import com.reandroid.archive.ByteInputSource;
import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.utils.io.FileUtil;

import java.io.*;

public interface DexSource<T> extends Comparable<DexSource<?>>{

    String getName();
    InputStream openStream() throws IOException;
    void write(byte[] bytes) throws IOException;
    boolean delete();
    T get();
    void set(T item);

    default int getDexFileNumber(){
        return DexFile.getDexFileNumber(getName());
    }
    default String getSimpleName() {
        String name = getName();
        int i = name.lastIndexOf('/');
        if(i < 0){
            i = name.lastIndexOf('\\');
        }
        if(i >= 0){
            name = name.substring(i + 1);
        }
        return name;
    }
    default DexSource<T> createNext(){
        throw new RuntimeException("Not implemented");
    }
    @Override
    default int compareTo(DexSource<?> dexSource){
        return Integer.compare(getDexFileNumber(), dexSource.getDexFileNumber());
    }

    static<T> DexSource<T> create(ZipEntryMap zipEntryMap, String name){
        return new ZipDexSource<>(zipEntryMap, name);
    }
    static<T> DexSource<T> create(File file){
        return new FileDexSource<>(file);
    }
    abstract class DexSourceImpl<T> implements DexSource<T> {

        private T item;
        @Override
        public T get() {
            return item;
        }
        @Override
        public void set(T item) {
            this.item = item;
        }

        @Override
        public boolean delete() {
            set(null);
            return onDelete();
        }
        abstract boolean onDelete();

        @Override
        public boolean equals(Object obj) {
            if(this == obj) {
                return true;
            }
            if(!(obj instanceof DexSource)) {
                return false;
            }
            DexSource<?> dexSource = (DexSource<?>) obj;
            return getDexFileNumber() == dexSource.getDexFileNumber();
        }
        @Override
        public int hashCode() {
            return getDexFileNumber();
        }
        @Override
        public String toString() {
            return getSimpleName();
        }
    }
    class FileDexSource<T> extends DexSourceImpl<T> {

        private final File file;

        public FileDexSource(File file) {
            super();
            this.file = file;
        }

        public File getFile() {
            return file;
        }

        @Override
        public String getSimpleName() {
            return getFile().getName();
        }

        @Override
        public String getName() {
            return getFile().getAbsolutePath();
        }

        @Override
        boolean onDelete() {
            File file = getFile();
            if(file.isFile()){
                return file.delete();
            }
            return true;
        }
        @Override
        public InputStream openStream() throws IOException {
            return new FileInputStream(getFile());
        }

        @Override
        public void write(byte[] bytes) throws IOException {
            OutputStream outputStream = FileUtil.outputStream(getFile());
            outputStream.write(bytes, 0, bytes.length);
            outputStream.close();
        }

        @Override
        public FileDexSource<T> createNext(){
            String name = DexFile.getDexName(getDexFileNumber() + 1);
            File dir = getFile().getParentFile();
            File file;
            if(dir == null){
                file = new File(name);
            }else {
                file = new File(dir, name);
            }
            return new FileDexSource<>(file);
        }
    }
    class ZipDexSource<T> extends DexSourceImpl<T> {

        private final ZipEntryMap zipEntryMap;
        private final String name;

        public ZipDexSource(ZipEntryMap zipEntryMap, String name) {
            super();
            this.zipEntryMap = zipEntryMap;
            this.name = name;
        }

        @Override
        public String getName() {
            return name;
        }
        @Override
        boolean onDelete() {
            this.zipEntryMap.remove(getName());
            return true;
        }
        @Override
        public InputStream openStream() throws IOException {
            InputSource inputSource = zipEntryMap.getInputSource(getName());
            if(inputSource == null){
                throw new IOException("Zip input source not found: " + getName());
            }
            return inputSource.openStream();
        }
        @Override
        public void write(byte[] bytes) throws IOException {
            ByteInputSource inputSource = new ByteInputSource(bytes, getName());
            zipEntryMap.add(inputSource);
        }

        @Override
        public ZipDexSource<T> createNext(){
            String name = FileUtil.combineUnixPath(FileUtil.getParent(getName()),
                    DexFile.getDexName(getDexFileNumber() + 1));
            return new ZipDexSource<>(zipEntryMap, name);
        }
        @Override
        public String toString() {
            return zipEntryMap.getModuleName() + ":/" + getSimpleName();
        }
    }
}
