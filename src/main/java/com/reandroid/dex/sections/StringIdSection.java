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
package com.reandroid.dex.sections;

import com.reandroid.arsc.base.Block;
import com.reandroid.dex.base.IntegerPair;
import com.reandroid.dex.data.StringData;
import com.reandroid.dex.id.StringId;
import com.reandroid.dex.id.TypeId;
import com.reandroid.dex.key.Key;
import com.reandroid.dex.key.StringKey;
import com.reandroid.dex.key.TypeKey;
import com.reandroid.utils.collection.CollectionUtil;
import com.reandroid.utils.collection.ComputeIterator;

import java.util.Iterator;
import java.util.function.Function;

public class StringIdSection extends IdSection<StringId> {

    StringIdSection(IntegerPair countAndOffset, SectionType<StringId> sectionType) {
        super(sectionType, new StringIdArray(countAndOffset, sectionType.getCreator()));
    }

    @Override
    public StringIdArray getItemArray() {
        return (StringIdArray) super.getItemArray();
    }

    @Override
    protected void onPreRefresh() {
        CollectionUtil.walk(Marker.parse(this));
        super.onPreRefresh();
    }
    @Override
    boolean keyChanged(Block block, Key key, boolean immediateSort) {
        boolean changed = super.keyChanged(block, key, immediateSort);
        if(key instanceof StringKey){
            String text = ((StringKey)key).getString();
            if(text.length() > 0){
                char ch = text.charAt(0);
                if(ch == 'L' || ch == '['){
                    updateTypeId(new TypeKey(text));
                }
            }
        }
        return changed;
    }
    private void updateTypeId(TypeKey typeKey){
        SectionList sectionList = getSectionList();
        if(sectionList == null){
            return;
        }
        TypeId typeId = sectionList.getSectionItem(SectionType.TYPE_ID, typeKey);
        if(typeId != null){
            // getKey() call triggers keyChanged event
            typeId.getKey();
        }
    }

    @Override
    void sortImmediate(StringId item) {
        StringData stringData = item.getStringData();
        stringData.getSection(SectionType.STRING_DATA).sortImmediate(stringData);
        super.sortImmediate(item);
    }

    private Iterator<StringKey> getAllStringKeys(Key key){
        Iterator<? extends Key> iterator = key.mentionedKeys();
        return ComputeIterator.of(iterator, (Function<Key, StringKey>) key1 -> {
            if(key1 instanceof StringKey){
                return (StringKey) key1;
            }
            if(key1 instanceof TypeKey){
                return new StringKey(((TypeKey) key1).getTypeName());
            }
            return null;
        });
    }
}
