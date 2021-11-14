package com.reandroid.lib.arsc.chunk;

import com.reandroid.lib.arsc.array.SpecTypePairArray;
import com.reandroid.lib.arsc.base.Block;
import com.reandroid.lib.arsc.container.PackageLastBlocks;
import com.reandroid.lib.arsc.container.SpecTypePair;
import com.reandroid.lib.arsc.group.EntryGroup;
import com.reandroid.lib.arsc.group.ItemGroup;
import com.reandroid.lib.arsc.item.IntegerItem;
import com.reandroid.lib.arsc.item.PackageName;
import com.reandroid.lib.arsc.pool.SpecStringPool;
import com.reandroid.lib.arsc.pool.TypeStringPool;
import com.reandroid.lib.arsc.value.EntryBlock;
import com.reandroid.lib.arsc.value.LibraryInfo;

import java.util.*;


public class PackageBlock extends BaseChunk {
    private final IntegerItem mPackageId;
    private final PackageName mPackageName;

    private final IntegerItem mTypeStrings;
    private final IntegerItem mLastPublicType;
    private final IntegerItem mKeyStrings;
    private final IntegerItem mLastPublicKey;
    private final IntegerItem mTypeIdOffset;

    private final TypeStringPool mTypeStringPool;
    private final SpecStringPool mSpecStringPool;

    private final SpecTypePairArray mSpecTypePairArray;
    private final LibraryBlock mLibraryBlock;

    private final PackageLastBlocks mPackageLastBlocks;

    private final Map<Integer, EntryGroup> mEntriesGroup;

    public PackageBlock() {
        super(ChunkType.PACKAGE, 3);
        this.mPackageId=new IntegerItem();
        this.mPackageName=new PackageName();
        this.mTypeStrings=new IntegerItem();
        this.mLastPublicType=new IntegerItem();
        this.mKeyStrings=new IntegerItem();
        this.mLastPublicKey=new IntegerItem();
        this.mTypeIdOffset=new IntegerItem();

        this.mTypeStringPool=new TypeStringPool(false);
        this.mSpecStringPool=new SpecStringPool(true);

        this.mSpecTypePairArray=new SpecTypePairArray();
        this.mLibraryBlock=new LibraryBlock();
        this.mPackageLastBlocks=new PackageLastBlocks(mSpecTypePairArray, mLibraryBlock);

        this.mEntriesGroup=new HashMap<>();

        addToHeader(mPackageId);
        addToHeader(mPackageName);
        addToHeader(mTypeStrings);
        addToHeader(mLastPublicType);
        addToHeader(mKeyStrings);
        addToHeader(mLastPublicKey);
        addToHeader(mTypeIdOffset);

        addChild(mTypeStringPool);
        addChild(mSpecStringPool);

        addChild(mPackageLastBlocks);

    }
    public int getId(){
        return mPackageId.get();
    }
    public void setId(int id){
        mPackageId.set(id);
    }
    public String getName(){
        return mPackageName.get();
    }
    public void setName(String name){
        mPackageName.set(name);
    }


    public TableBlock getTableBlock(){
        Block parent=getParent();
        while(parent!=null){
            if(parent instanceof TableBlock){
                return (TableBlock)parent;
            }
            parent=parent.getParent();
        }
        return null;
    }
    public int getPackageId(){
        return mPackageId.get();
    }
    public int setPackageId(){
        return mPackageId.get();
    }
    public String getPackageName(){
        return mPackageName.get();
    }
    public void setPackageName(String name){
        mPackageName.set(name);
    }
    public void setTypeStrings(int i){
        mTypeStrings.set(i);
    }
    public int getLastPublicType(){
        return mLastPublicType.get();
    }
    public void setLastPublicType(int i){
        mLastPublicType.set(i);
    }
    public int getKeyStrings(){
        return mKeyStrings.get();
    }
    public void setKeyStrings(int i){
        mKeyStrings.set(i);
    }
    public int getLastPublicKey(){
        return mLastPublicKey.get();
    }
    public void setLastPublicKey(int i){
        mLastPublicKey.set(i);
    }
    public int getTypeIdOffset(){
        return mTypeIdOffset.get();
    }
    public void setTypeIdOffset(int i){
        mTypeIdOffset.set(i);
    }
    public TypeStringPool getTypeStringPool(){
        return mTypeStringPool;
    }
    public SpecStringPool getSpecStringPool(){
        return mSpecStringPool;
    }
    public SpecTypePairArray getSpecTypePairArray(){
        return mSpecTypePairArray;
    }
    public List<LibraryInfo> listLibraryInfo(){
        return mLibraryBlock.listLibraryInfo();
    }

    public void addLibrary(LibraryBlock libraryBlock){
        if(libraryBlock==null){
            return;
        }
        LibraryInfo[] allInfo=libraryBlock.getAllInfo();
        if (allInfo==null){
            return;
        }
        for(LibraryInfo info:allInfo){
            addLibraryInfo(info);
        }
    }
    public void addLibraryInfo(LibraryInfo info){
        mLibraryBlock.addLibraryInfo(info);
    }
    public Set<Integer> listResourceIds(){
        return mEntriesGroup.keySet();
    }
    public Collection<EntryGroup> listEntryGroup(){
        return mEntriesGroup.values();
    }
    public EntryGroup getEntryGroup(int resId){
        return mEntriesGroup.get(resId);
    }
    private void updateEntryGroup(EntryBlock entryBlock){
        int resId=entryBlock.getResourceId();
        EntryGroup group=mEntriesGroup.get(resId);
        if(group==null){
            group=new EntryGroup(resId);
            mEntriesGroup.put(resId, group);
        }
        group.add(entryBlock);
    }
    public List<EntryBlock> listEntries(byte typeId, int entryId){
        List<EntryBlock> results=new ArrayList<>();
        for(SpecTypePair pair:listSpecTypePair(typeId)){
            results.addAll(pair.listEntries(entryId));
        }
        return results;
    }
    public List<SpecTypePair> listSpecTypePair(byte typeId){
        List<SpecTypePair> results=new ArrayList<>();
        for(SpecTypePair pair:listAllSpecTypePair()){
            if(typeId==pair.getTypeId()){
                results.add(pair);
            }
        }
        return results;
    }
    public List<SpecTypePair> listAllSpecTypePair(){
        return getSpecTypePairArray().listItems();
    }

    private void refreshKeyStrings(){
        int pos=countUpTo(mSpecStringPool);
        mKeyStrings.set(pos);
    }
    public void onEntryAdded(EntryBlock entryBlock){
        updateEntryGroup(entryBlock);
    }
    @Override
    public void onChunkLoaded() {
    }

    @Override
    protected void onChunkRefreshed() {
        refreshKeyStrings();
    }

    @Override
    public String toString(){
        StringBuilder builder=new StringBuilder();
        builder.append(super.toString());
        builder.append(", id=");
        builder.append(String.format("0x%02x", getId()));
        builder.append(", name=");
        builder.append(getName());
        int libCount=mLibraryBlock.getLibraryCount();
        if(libCount>0){
            builder.append(", libraries=");
            builder.append(libCount);
        }
        return builder.toString();
    }
}
