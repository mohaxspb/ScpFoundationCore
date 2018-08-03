package ru.kuchanov.scpcore.db.model;

import java.util.ArrayList;
import java.util.List;

import io.realm.RealmObject;

/**
 * Created by y.kuchanov on 28.12.16.
 * <p>
 * We need this wrapper, because Realm unable to store Strings in arrays/lists
 */
public class RealmString extends RealmObject{

    public String val;

    public RealmString() {
    }

    public RealmString(String val) {
        this.val = val;
    }

    public String getVal() {
        return val;
    }

    public void setVal(String val) {
        this.val = val;
    }

    @Override
    public String toString() {
        return val;
    }

    public static List<String> toStringList(List<RealmString> realmStrings) {
        if (realmStrings == null) {
            return null;
        }
        List<String> strings = new ArrayList<>();
        for (RealmString realmString : realmStrings) {
            strings.add(realmString.val);
        }
        return strings;
    }
}