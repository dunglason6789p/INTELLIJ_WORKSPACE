/*
 * Decompiled with CFR 0.146.
 */
package third_party.org.chokkan.crfsuite;

import third_party.org.chokkan.crfsuite.Attribute;
import third_party.org.chokkan.crfsuite.Item;
import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.Tagger;
import third_party.org.chokkan.crfsuite.Trainer;

public class crfsuiteJNI {
    public static final native long new_Item__SWIG_0();

    public static final native long new_Item__SWIG_1(long var0);

    public static final native long Item_size(long var0, Item var2);

    public static final native long Item_capacity(long var0, Item var2);

    public static final native void Item_reserve(long var0, Item var2, long var3);

    public static final native boolean Item_isEmpty(long var0, Item var2);

    public static final native void Item_clear(long var0, Item var2);

    public static final native void Item_add(long var0, Item var2, long var3, Attribute var5);

    public static final native long Item_get(long var0, Item var2, int var3);

    public static final native void Item_set(long var0, Item var2, int var3, long var4, Attribute var6);

    public static final native void delete_Item(long var0);

    public static final native long new_ItemSequence__SWIG_0();

    public static final native long new_ItemSequence__SWIG_1(long var0);

    public static final native long ItemSequence_size(long var0, ItemSequence var2);

    public static final native long ItemSequence_capacity(long var0, ItemSequence var2);

    public static final native void ItemSequence_reserve(long var0, ItemSequence var2, long var3);

    public static final native boolean ItemSequence_isEmpty(long var0, ItemSequence var2);

    public static final native void ItemSequence_clear(long var0, ItemSequence var2);

    public static final native void ItemSequence_add(long var0, ItemSequence var2, long var3, Item var5);

    public static final native long ItemSequence_get(long var0, ItemSequence var2, int var3);

    public static final native void ItemSequence_set(long var0, ItemSequence var2, int var3, long var4, Item var6);

    public static final native void delete_ItemSequence(long var0);

    public static final native long new_StringList__SWIG_0();

    public static final native long new_StringList__SWIG_1(long var0);

    public static final native long StringList_size(long var0, StringList var2);

    public static final native long StringList_capacity(long var0, StringList var2);

    public static final native void StringList_reserve(long var0, StringList var2, long var3);

    public static final native boolean StringList_isEmpty(long var0, StringList var2);

    public static final native void StringList_clear(long var0, StringList var2);

    public static final native void StringList_add(long var0, StringList var2, String var3);

    public static final native String StringList_get(long var0, StringList var2, int var3);

    public static final native void StringList_set(long var0, StringList var2, int var3, String var4);

    public static final native void delete_StringList(long var0);

    public static final native void Attribute_attr_set(long var0, Attribute var2, String var3);

    public static final native String Attribute_attr_get(long var0, Attribute var2);

    public static final native void Attribute_value_set(long var0, Attribute var2, double var3);

    public static final native double Attribute_value_get(long var0, Attribute var2);

    public static final native long new_Attribute__SWIG_0();

    public static final native long new_Attribute__SWIG_1(String var0);

    public static final native long new_Attribute__SWIG_2(String var0, double var1);

    public static final native void delete_Attribute(long var0);

    public static final native long new_Trainer();

    public static final native void delete_Trainer(long var0);

    public static final native void Trainer_clear(long var0, Trainer var2);

    public static final native void Trainer_append(long var0, Trainer var2, long var3, ItemSequence var5, long var6, StringList var8, int var9);

    public static final native boolean Trainer_select(long var0, Trainer var2, String var3, String var4);

    public static final native int Trainer_train(long var0, Trainer var2, String var3, int var4);

    public static final native long Trainer_params(long var0, Trainer var2);

    public static final native void Trainer_set(long var0, Trainer var2, String var3, String var4);

    public static final native String Trainer_get(long var0, Trainer var2, String var3);

    public static final native String Trainer_help(long var0, Trainer var2, String var3);

    public static final native void Trainer_message(long var0, Trainer var2, String var3);

    public static final native void Trainer_messageSwigExplicitTrainer(long var0, Trainer var2, String var3);

    public static final native void Trainer_director_connect(Trainer var0, long var1, boolean var3, boolean var4);

    public static final native void Trainer_change_ownership(Trainer var0, long var1, boolean var3);

    public static final native long new_Tagger();

    public static final native void delete_Tagger(long var0);

    public static final native boolean Tagger_open(long var0, Tagger var2, String var3);

    public static final native void Tagger_close(long var0, Tagger var2);

    public static final native long Tagger_labels(long var0, Tagger var2);

    public static final native long Tagger_tag(long var0, Tagger var2, long var3, ItemSequence var5);

    public static final native void Tagger_set(long var0, Tagger var2, long var3, ItemSequence var5);

    public static final native long Tagger_viterbi(long var0, Tagger var2);

    public static final native double Tagger_probability(long var0, Tagger var2, long var3, StringList var5);

    public static final native double Tagger_marginal(long var0, Tagger var2, String var3, int var4);

    public static final native String version();

    public static void SwigDirector_Trainer_message(Trainer self, String msg) {
        self.message(msg);
    }

    private static final native void swig_module_init();

    static {
        crfsuiteJNI.swig_module_init();
    }
}

