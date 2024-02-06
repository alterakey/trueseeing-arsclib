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
package com.reandroid.dex.smali.model;

import com.reandroid.dex.smali.SmaliReader;
import com.reandroid.dex.smali.SmaliWriter;

import java.io.IOException;

public class SmaliAnnotationElement extends Smali{

    private String name;
    private SmaliValue value;

    public SmaliAnnotationElement(){
        super();
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = name;
    }
    public SmaliValue getValue() {
        return value;
    }
    public void setValue(SmaliValue value) {
        this.value = value;
        if(value != null){
            value.setParent(this);
        }
    }

    @Override
    public void append(SmaliWriter writer) throws IOException {
        writer.append(getName());
        writer.append(" = ");
        getValue().append(writer);
    }

    @Override
    public void parse(SmaliReader reader) throws IOException{
        reader.skipWhitespaces();
        int i1 = reader.indexOfWhiteSpace();
        int i2 = reader.indexOf('=');
        int i;
        if(i1 < i2){
            i = i1;
        }else {
            i = i2;
        }
        int length = i - reader.position();
        setName(reader.readString(length));
        reader.skipWhitespaces();
        if(reader.readASCII() != '='){
            // throw
        }
        reader.skipWhitespaces();
        SmaliValue smaliValue = SmaliValue.create(reader);
        setValue(smaliValue);
        smaliValue.parse(reader);
    }
}
