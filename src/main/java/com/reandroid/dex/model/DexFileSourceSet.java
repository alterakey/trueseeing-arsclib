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

import com.reandroid.archive.InputSource;
import com.reandroid.archive.ZipEntryMap;
import com.reandroid.utils.CompareUtil;
import com.reandroid.utils.collection.ArrayCollection;
import com.reandroid.utils.collection.ComputeIterator;
import com.reandroid.utils.io.FileUtil;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.function.Predicate;

public class DexFileSourceSet implements Iterable<DexSource<DexFile>>{

    private final ArrayCollection<DexSource<DexFile>> sourceList;
    private boolean mReadStringsMode;

    public DexFileSourceSet(){
        this.sourceList = new ArrayCollection<>();
    }

    public void merge(DexFileSourceSet sourceSet){
        if(sourceSet == this){
            throw new IllegalArgumentException("Cyclic merge");
        }
        for(DexSource<DexFile> coming : sourceSet){
            if(isEmpty(coming)){
                continue;
            }
            DexSource<DexFile> source = createNext();
            source.set(coming.get());
            try {
                save(source);
            } catch (IOException exception) {
                throw new RuntimeException(exception);
            }
        }
    }
    public void removeEmpty() {
        Iterator<DexSource<DexFile>> iterator = sourceList.clonedIterator();
        while (iterator.hasNext()){
            DexSource<DexFile> source = iterator.next();
            if(isEmpty(source)){
                remove(source);
            }
        }
    }
    public void saveAll(File dir) throws IOException {
        Iterator<DexSource<DexFile>> iterator = sourceList.clonedIterator();
        while (iterator.hasNext()){
            DexSource<DexFile> source = iterator.next();
            File file = new File(dir, source.getSimpleName());
            DexSource<DexFile> dexSource = DexSource.create(file);
            dexSource.set(source.get());
            if(isEmpty(dexSource)){
                dexSource.delete();
            }else {
                dexSource.write(source.get().getBytes());
            }
        }
    }
    public void saveAll() throws IOException {
        Iterator<DexSource<DexFile>> iterator = sourceList.clonedIterator();
        while (iterator.hasNext()){
            save(iterator.next());
        }
    }
    private void save(DexSource<DexFile> source) throws IOException {
        if(isEmpty(source)){
            delete(source);
            return;
        }
        DexFile dexFile = source.get();
        source.write(dexFile.getBytes());
    }
    private boolean isEmpty(DexSource<DexFile> source){
        DexFile dexFile = source.get();
        return dexFile == null || dexFile.isEmpty();
    }
    public Iterator<DexFile> getClonedDexFiles() {
        return ComputeIterator.of(clonedIterator(), DexSource::get);
    }
    public Iterator<DexFile> getDexFiles() {
        return ComputeIterator.of(iterator(), DexSource::get);
    }
    @Override
    public Iterator<DexSource<DexFile>> iterator() {
        return sourceList.iterator();
    }
    public Iterator<DexSource<DexFile>> clonedIterator() {
        return sourceList.clonedIterator();
    }

    public void addAll(ZipEntryMap zipEntryMap) throws IOException {
        addAll(zipEntryMap, (String) null);
    }
    public void addAll(ZipEntryMap zipEntryMap, String directory) throws IOException {
        String path = "";
        if(directory != null && directory.length() > 0){
            if(directory.charAt(0) == '/'){
                directory = directory.substring(1);
            }
            path = directory;
        }
        if(path.length() > 0 && !path.endsWith("/")){
            path = path + "/";
        }
        final String pathPrefix = path + "classes";
        Predicate<InputSource> filter = inputSource -> {
            String name = inputSource.getAlias();

            return name.startsWith(pathPrefix) && DexFile.getDexFileNumber(name) >= 0;
        };
        addAll(zipEntryMap, filter);
    }
    public void addAll(ZipEntryMap zipEntryMap, Predicate<InputSource> filter) throws IOException {
        addAll(zipEntryMap, zipEntryMap.iterator(filter));
    }
    public void addAll(ZipEntryMap zipEntryMap, Iterator<InputSource> iterator) throws IOException {
        while (iterator.hasNext()){
            add(zipEntryMap, iterator.next().getAlias());
        }
    }
    public void add(ZipEntryMap zipEntryMap, InputSource inputSource) throws IOException {
        String name = inputSource.getAlias();
        if(zipEntryMap.getInputSource(name) == null){
            zipEntryMap.add(inputSource);
        }
        add(zipEntryMap, name);
    }
    public void add(ZipEntryMap zipEntryMap, String name) throws IOException {
        add(DexSource.create(zipEntryMap, name));
    }
    public void addAll(File dir) throws IOException {
        if(!dir.isDirectory()){
            throw new IOException("No such directory: " + dir);
        }
        File[] files = dir.listFiles();
        if(files == null){
            return;
        }
        for(File file : files){
            if(!file.isFile()){
                continue;
            }
            if(DexFile.getDexFileNumber(file.getName()) < 0){
                continue;
            }
            add(file);
        }
    }
    public DexSource<DexFile> add(File file) throws IOException {
        return add(DexSource.create(file));
    }
    public DexSource<DexFile> add(DexSource<DexFile> source) throws IOException {
        DexSource<DexFile> exist = sourceList.getElement(source);
        if(exist != null){
            if(exist == source){
                load(source);
                return exist;
            }
            throw new IOException("Duplicate dex source: " + source);
        }
        load(source);
        sourceList.remove(source);
        sourceList.add(source);
        sourceList.sort(CompareUtil.getComparableComparator());
        return source;
    }
    public DexFile getDexFile(int index){
        if(index < 0 || index >= size()){
            return null;
        }
        return getSource(index).get();
    }
    public DexSource<DexFile> getSource(int index){
        return sourceList.get(index);
    }
    public DexSource<DexFile> getSource(DexFile dexFile){
        if(dexFile != null){
            for(DexSource<DexFile> source : this){
                if(dexFile == source.get()){
                    return source;
                }
            }
        }
        return null;
    }
    public DexSource<DexFile> getFirst(){
        return sourceList.getFirst();
    }
    public DexSource<DexFile> getLast(){
        sourceList.sort(CompareUtil.getComparableComparator());
        return sourceList.getLast();
    }
    public DexSource<DexFile> createNext(){
        DexSource<DexFile> last = getLast();
        DexSource<DexFile> source = last.createNext();
        sourceList.add(source);
        sourceList.sort(CompareUtil.getComparableComparator());
        return source;
    }
    public String buildNextName(){
        DexSource<DexFile> last = getLast();
        if(last == null){
            return DexFile.getDexName(0);
        }
        String dir = FileUtil.getParent(last.getName());
        String name = DexFile.getDexName(last.getDexFileNumber() + 1);
        return FileUtil.combineUnixPath(dir, name);
    }

    public int size(){
        return sourceList.size();
    }
    public void delete(DexSource<DexFile> dexSource){
        remove(dexSource);
        dexSource.delete();
    }
    public void remove(DexSource<DexFile> dexSource){
        sourceList.remove(dexSource);
        dexSource.set(null);
    }

    public boolean isReadStringsMode() {
        return mReadStringsMode;
    }
    public void setReadStringsMode(boolean readStringsMode) {
        this.mReadStringsMode = readStringsMode;
    }
    private void load(DexSource<DexFile> dexSource) throws IOException {
        DexFile dexFile = dexSource.get();
        if(dexFile != null){
            return;
        }
        if(mReadStringsMode){
            dexFile = DexFile.readStrings(dexSource.openStream());
        }else {
            dexFile = DexFile.read(dexSource.openStream());
        }
        dexSource.set(dexFile);
        dexFile.setSimpleName(dexSource.toString());
    }

    @Override
    public String toString() {
        return "size = " + size();
    }
}
