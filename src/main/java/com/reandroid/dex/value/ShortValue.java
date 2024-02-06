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
package com.reandroid.dex.value;

import com.reandroid.dex.smali.model.SmaliValue;
import com.reandroid.dex.smali.model.SmaliValueShort;
import com.reandroid.utils.HexUtil;

public class ShortValue extends PrimitiveValue {

    public ShortValue() {
        super(DexValueType.SHORT);
    }

    public short get(){
        return (short) getNumberValue();
    }
    public void set(short value){
        setNumberValue(value & 0xffff);
    }
    @Override
    public DexValueType<?> getValueType() {
        return DexValueType.SHORT;
    }
    @Override
    public String getHex() {
        return HexUtil.toHex(getNumberValue(), getValueSize()) + "S";
    }

    @Override
    public void fromSmali(SmaliValue smaliValue) {
        SmaliValueShort smaliValueShort = (SmaliValueShort) smaliValue;
        set(smaliValueShort.getValue());
    }
}
