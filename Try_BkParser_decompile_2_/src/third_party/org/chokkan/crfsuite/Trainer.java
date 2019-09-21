/*
 * Decompiled with CFR 0.146.
 */
package third_party.org.chokkan.crfsuite;

import third_party.org.chokkan.crfsuite.ItemSequence;
import third_party.org.chokkan.crfsuite.StringList;
import third_party.org.chokkan.crfsuite.crfsuiteJNI;

public class Trainer {
    private long swigCPtr;
    protected boolean swigCMemOwn;

    public Trainer(long cPtr, boolean cMemoryOwn) {
        this.swigCMemOwn = cMemoryOwn;
        this.swigCPtr = cPtr;
    }

    public static long getCPtr(Trainer obj) {
        return obj == null ? 0L : obj.swigCPtr;
    }

    protected void finalize() {
        this.delete();
    }

    public synchronized void delete() {
        if (this.swigCPtr != 0L) {
            if (this.swigCMemOwn) {
                this.swigCMemOwn = false;
                crfsuiteJNI.delete_Trainer(this.swigCPtr);
            }
            this.swigCPtr = 0L;
        }
    }

    protected void swigDirectorDisconnect() {
        this.swigCMemOwn = false;
        this.delete();
    }

    public void swigReleaseOwnership() {
        this.swigCMemOwn = false;
        crfsuiteJNI.Trainer_change_ownership(this, this.swigCPtr, false);
    }

    public void swigTakeOwnership() {
        this.swigCMemOwn = true;
        crfsuiteJNI.Trainer_change_ownership(this, this.swigCPtr, true);
    }

    public Trainer() {
        this(crfsuiteJNI.new_Trainer(), true);
        crfsuiteJNI.Trainer_director_connect(this, this.swigCPtr, this.swigCMemOwn, true);
    }

    public void clear() {
        crfsuiteJNI.Trainer_clear(this.swigCPtr, this);
    }

    public void append(ItemSequence xseq, StringList yseq, int group) {
        crfsuiteJNI.Trainer_append(this.swigCPtr, this, ItemSequence.getCPtr(xseq), xseq, StringList.getCPtr(yseq), yseq, group);
    }

    public boolean select(String algorithm, String type) {
        return crfsuiteJNI.Trainer_select(this.swigCPtr, this, algorithm, type);
    }

    public int train(String model, int holdout) {
        return crfsuiteJNI.Trainer_train(this.swigCPtr, this, model, holdout);
    }

    public StringList params() {
        return new StringList(crfsuiteJNI.Trainer_params(this.swigCPtr, this), true);
    }

    public void set(String name, String value) {
        crfsuiteJNI.Trainer_set(this.swigCPtr, this, name, value);
    }

    public String get(String name) {
        return crfsuiteJNI.Trainer_get(this.swigCPtr, this, name);
    }

    public String help(String name) {
        return crfsuiteJNI.Trainer_help(this.swigCPtr, this, name);
    }

    public void message(String msg) {
        if (this.getClass() == Trainer.class) {
            crfsuiteJNI.Trainer_message(this.swigCPtr, this, msg);
        } else {
            crfsuiteJNI.Trainer_messageSwigExplicitTrainer(this.swigCPtr, this, msg);
        }
    }
}

